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
                        Log.d("NotificationActionReceiver", "Activity completed: $activityTitle")
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
                        Log.d("NotificationActionReceiver", "Break reminder postponed for 10 minutes")
                    } else {
                        // Logic for postponing TODO tasks can be implemented later
                        Log.d("NotificationActionReceiver", "TODO reminder postponed")
                    }
                } catch (e: Exception) {
                    Log.e("NotificationActionReceiver", "Error postponing reminder: ${e.message}", e)
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
            // For now, just save statistics - task completion logic can be improved later
            if (isTodo) {
                // Future improvement: find and update the actual TODO task by title or ID
                Log.d("NotificationActionReceiver", "TODO task completed: $activityTitle")
            }
        } catch (e: Exception) {
            Log.e("NotificationActionReceiver", "Failed to save activity statistics: ${e.message}", e)
            throw e
        }
    }
}
