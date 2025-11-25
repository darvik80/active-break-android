package xyz.crearts.activebreak.ui.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.workers.BreakReminderWorker
import xyz.crearts.activebreak.workers.NotificationHelper
import java.util.concurrent.TimeUnit

@Composable
fun NotificationActionDialog(
    title: String,
    description: String?,
    isTodo: Boolean,
    onDismiss: () -> Unit,
    onActionCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isTodo) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isTodo) "Задача" else "Перерыв",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Выберите действие:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Complete button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                handleCompleteAction(context, title, isTodo)
                                onActionCompleted()
                                onDismiss()
                            } catch (e: Exception) {
                                Log.e("NotificationActionDialog", "Error completing action: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выполнено")
                }

                // Postpone button
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                handlePostponeAction(context, isTodo)
                                onActionCompleted()
                                onDismiss()
                            } catch (e: Exception) {
                                Log.e("NotificationActionDialog", "Error postponing: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отложить")
                }

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отмена")
                }
            }
        }
    )
}

// Handle complete action - same logic as NotificationActionReceiver
private suspend fun handleCompleteAction(context: Context, activityTitle: String, isTodo: Boolean) {
    // Dismiss notification
    NotificationHelper.dismissNotification(context)
    
    // Save statistics
    saveActivityStatistics(context, activityTitle, isTodo)
    Log.d("NotificationActionDialog", "Activity completed: $activityTitle")
}

// Handle postpone action - same logic as NotificationActionReceiver
private suspend fun handlePostponeAction(context: Context, isTodo: Boolean) {
    // Dismiss notification
    NotificationHelper.dismissNotification(context)
    
    // Reschedule reminder in 10 minutes
    if (!isTodo) {
        val workRequest = OneTimeWorkRequestBuilder<BreakReminderWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("NotificationActionDialog", "Break reminder postponed for 10 minutes")
    } else {
        // Logic for postponing TODO tasks can be implemented later
        Log.d("NotificationActionDialog", "TODO reminder postponed")
    }
}



// Save activity statistics - same logic as NotificationActionReceiver
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
                val tasks = todoDao.getAllTasks()
                // Since we can't use Flow in suspend function, we need to find task by title
                // This is a simple approach - in production, we should pass task ID through notification
                val matchingTask = todoDao.getFirstActiveTask()
                if (matchingTask != null && matchingTask.title == activityTitle) {
                    val repository = xyz.crearts.activebreak.data.repository.TodoTaskRepository(todoDao)
                    repository.toggleTaskCompletion(matchingTask)
                    Log.d("NotificationActionDialog", "TODO task marked as completed: $activityTitle")
                } else {
                    Log.w("NotificationActionDialog", "Could not find matching TODO task: $activityTitle")
                }
            } catch (e: Exception) {
                Log.e("NotificationActionDialog", "Error updating TODO task: ${e.message}", e)
            }
        }
    } catch (e: Exception) {
        Log.e("NotificationActionDialog", "Failed to save activity statistics: ${e.message}", e)
        throw e
    }
}
