package xyz.crearts.activebreak.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.flow.first
import xyz.crearts.activebreak.data.local.AppDatabase
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import xyz.crearts.activebreak.data.preferences.Settings
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.data.repository.BreakActivityRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = BreakActivityRepository(database.breakActivityDao())

            // Проверяем, находимся ли мы в активном временном диапазоне
            val settingsManager = SettingsManager.instance
            val settings = settingsManager.getSettings().first()

            if (!isWithinActiveHours(settings)) {
                return Result.success()
            }

            // Получаем случайную активность
            val activity = repository.getRandomActivity()

            activity?.let {
                NotificationHelper.showBreakNotification(
                    context = applicationContext,
                    activity = it
                )

                // Отправка в мессенджеры
                sendToMessengers(settings, it)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("BreakReminderWorker", "Error: ${e.message}", e)
            Result.failure()
        }
    }

    private fun isWithinActiveHours(settings: Settings): Boolean {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        val startTimeInMinutes = settings.startHour * 60 + settings.startMinute
        val endTimeInMinutes = settings.endHour * 60 + settings.endMinute

        return currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes
    }

    private suspend fun sendToMessengers(settings: Settings, activity: BreakActivity) {
        val message = MessengerHelper.formatBreakMessage(
            activity.title,
            activity.description
        )

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

        // WhatsApp (только через Intent, не автоматически)
        // Автоматическая отправка в WhatsApp невозможна без открытия приложения
    }

    companion object {
        const val WORK_NAME = "BreakReminderWork"

        fun scheduleWork(context: Context, intervalMinutes: Long) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BreakReminderWorker>(
                intervalMinutes, TimeUnit.MINUTES,
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
