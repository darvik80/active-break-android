package xyz.crearts.activebreak.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.flow.first
import xyz.crearts.activebreak.MainActivity
import xyz.crearts.activebreak.R
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

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(applicationContext)
    }

    override suspend fun doWork(): Result {
        // Use setForeground() for long-running tasks if necessary
        // But for this short task, we just run it.
        // If we want to show that it's running, we can promote it to foreground.
        setForeground(createForegroundInfo(applicationContext))

        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = BreakActivityRepository(database.breakActivityDao())

            // Проверяем, находимся ли мы в активном временном диапазоне
            val settingsManager = SettingsManager.instance
            val settings = settingsManager.getSettings().first()

            if (!isWithinActiveHours(settings)) {
                return Result.success()
            }
            
            // Если уведомления о перерывах отключены
            if (!settings.breakNotificationsEnabled) {
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

    private fun createForegroundInfo(context: Context): ForegroundInfo {
        val id = "break_service_channel"
        val title = "ActiveBreak"
        val cancel = "Остановить"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, title, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle("ActiveBreak работает")
            .setTicker(title)
            .setContentText("Ожидание времени перерыва")
            .setSmallIcon(R.drawable.ic_foreground) // Или другой ресурс иконки, если этот вектор
            .setOngoing(true)
            .build()

        // Для Android 14+ нужно указывать тип сервиса
        return if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
             ForegroundInfo(2001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        } else {
             ForegroundInfo(2001, notification)
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
