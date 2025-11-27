package xyz.crearts.activebreak.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.workers.BreakReminderWorker
import xyz.crearts.activebreak.workers.NotificationHelper
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val activityTitle = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_TITLE) ?: return
        val activityDescription = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_DESCRIPTION)
        val isTodo = intent.getBooleanExtra(NotificationHelper.EXTRA_IS_TODO, false)

        // Use goAsync() for safe coroutine handling in BroadcastReceiver
        val pendingResult = goAsync()

        when (intent.action) {
            NotificationHelper.ACTION_COMPLETED -> {
                // Close notification
                NotificationHelper.dismissNotification(context)

                // Save statistics asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        saveActivityStatistics(context, activityTitle, isTodo)
                    } catch (e: Exception) {
                        Log.e("NotificationActionReceiver", "Error saving statistics: ${e.message}", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            NotificationHelper.ACTION_POSTPONE -> {
                // Close notification
                NotificationHelper.dismissNotification(context)

                try {
                    // Reschedule reminder in 10 minutes
                    if (!isTodo) {
                        val workRequest = OneTimeWorkRequestBuilder<BreakReminderWorker>()
                            .setInitialDelay(10, TimeUnit.MINUTES)
                            .build()

                        WorkManager.getInstance(context).enqueue(workRequest)
                    } else {
                        // For TODO tasks, reschedule using TodoReminderWorker
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(context)
                                val todoDao = database.todoTaskDao()

                                // Find the task by title (in production, should pass task ID)
                                val matchingTask = todoDao.getFirstActiveTask()
                                if (matchingTask != null && matchingTask.title == activityTitle) {
                                    xyz.crearts.activebreak.workers.TodoReminderWorker.scheduleTodoReminder(
                                        context,
                                        matchingTask.copy(
                                            nextDueDate = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("NotificationActionReceiver", "Error postponing TODO reminder: ${e.message}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NotificationActionReceiver", "Error postponing reminder: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }

            NotificationHelper.ACTION_TOGGLE_STATUS -> {
                // Handle status bar notification toggle action
                try {
                    // Broadcast intent to MainActivity to handle status toggle
                    val toggleIntent = Intent("xyz.crearts.activebreak.ACTION_TOGGLE_APP_STATUS")
                    context.sendBroadcast(toggleIntent)

                } catch (e: Exception) {
                    Log.e("NotificationActionReceiver", "Error handling status toggle: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }

            else -> {
                pendingResult.finish()
            }
        }
    }



    private suspend fun saveActivityStatistics(context: Context, activityTitle: String, isTodo: Boolean) {
        try {
            val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(context)
            database.activityStatisticsDao().insert(
                xyz.crearts.activebreak.data.local.entity.ActivityStatistics(
                    activityTitle = activityTitle,
                    activityType = if (isTodo) "TODO" else "BREAK"
                )
            )

            // If this is a TODO, mark the task as completed
            if (isTodo) {
                try {
                    // Find and update the actual TODO task by title
                    val todoDao = database.todoTaskDao()
                    // Since we can't use Flow in suspend function, we need to find task by title
                    // This is a simple approach - in production, we should pass task ID through notification
                    val matchingTask = todoDao.getFirstActiveTask()
                    if (matchingTask != null && matchingTask.title == activityTitle) {
                        val repository = xyz.crearts.activebreak.data.repository.TodoTaskRepository(todoDao)
                        repository.toggleTaskCompletion(matchingTask)
                    }
                } catch (e: Exception) {
                    Log.e("NotificationActionReceiver", "Error updating TODO task: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationActionReceiver", "Failed to save activity statistics: ${e.message}", e)
            throw e
        }
    }
}
