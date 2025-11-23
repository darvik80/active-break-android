package xyz.crearts.activebreak.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import xyz.crearts.activebreak.data.local.entity.TodoTask
import xyz.crearts.activebreak.data.preferences.SettingsManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class TodoReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(KEY_TASK_ID, -1)
            if (taskId == -1L) return Result.failure()

            val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(applicationContext)
            val task = database.todoTaskDao().getTaskById(taskId) ?: return Result.failure()

            if (task.isCompleted) return Result.success()

            // Получаем настройки
            val settings = SettingsManager.instance.getSettings().first()

            // Показываем уведомление
            NotificationHelper.showTodoNotification(applicationContext, task)

            // Отправка в мессенджеры
            val message = MessengerHelper.formatTodoMessage(task.title, task.description)

            // Telegram
            if (settings.telegramEnabled &&
                settings.telegramBotToken.isNotBlank() &&
                settings.telegramChatId.isNotBlank()) {
                MessengerHelper.sendToTelegram(
                    settings.telegramBotToken,
                    settings.telegramChatId,
                    message
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("TodoReminderWorker", "Error: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        private const val KEY_TASK_ID = "task_id"

        fun scheduleTodoReminder(context: Context, task: TodoTask) {
            val inputData = workDataOf(KEY_TASK_ID to task.id)
            
            // Если у задачи есть deadline, можно использовать его для точного времени,
            // но пока просто запускаем сейчас (или можно добавить delay)
            val workRequest = OneTimeWorkRequestBuilder<TodoReminderWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
