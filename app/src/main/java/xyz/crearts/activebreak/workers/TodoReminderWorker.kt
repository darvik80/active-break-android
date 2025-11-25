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

            if (task.isCompleted || task.isPaused) return Result.success()

            // Получаем настройки
            val settings = SettingsManager.instance.getSettings().first()

            // Если уведомления о задачах отключены, ничего не делаем
            if (!settings.todoNotificationsEnabled) return Result.success()

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

            // Если задача периодическая, нужно запланировать следующий запуск
            // (Эта логика может быть здесь или при отметке выполнения, но напоминание срабатывает 1 раз для текущего nextDueDate)

            Result.success()
        } catch (e: Exception) {
            Log.e("TodoReminderWorker", "Error: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        private const val KEY_TASK_ID = "task_id"

        fun scheduleTodoReminder(context: Context, task: TodoTask) {
            // Отменяем предыдущие напоминания для этой задачи, чтобы не дублировать
            // WorkManager.getInstance(context).cancelAllWorkByTag("todo_${task.id}")
            // (но мы используем OneTimeWork, так что это не критично, если id уникален, но лучше добавить tag)

            val inputData = workDataOf(KEY_TASK_ID to task.id)

            val dueDate = task.nextDueDate ?: task.dueDate ?: return
            val reminderTime = dueDate - (task.reminderMinutesBefore * 60 * 1000)
            val delay = reminderTime - System.currentTimeMillis()

            if (delay < 0) {
                // Время напоминания уже прошло
                // Можно запустить сразу, если оно было недавно, или пропустить
                return
            }

            val workRequest = OneTimeWorkRequestBuilder<TodoReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("todo_${task.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "todo_reminder_${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelTodoReminder(context: Context, taskId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("todo_reminder_$taskId")
        }
    }
}
