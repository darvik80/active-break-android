package xyz.crearts.activebreak.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xyz.crearts.activebreak.MainActivity
import xyz.crearts.activebreak.R
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import xyz.crearts.activebreak.data.local.entity.TodoTask
import xyz.crearts.activebreak.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_ID = "break_reminders"
    private const val CHANNEL_NAME = "Break Reminders"
    const val STATUS_CHANNEL_ID = "status_bar"
    private const val STATUS_CHANNEL_NAME = "ActiveBreak Status"
    private const val NOTIFICATION_ID = 1001
    private const val TODO_NOTIFICATION_ID = 1002
    private const val STATUS_NOTIFICATION_ID = 1003

    const val ACTION_COMPLETED = "xyz.crearts.activebreak.ACTION_COMPLETED"
    const val ACTION_POSTPONE = "xyz.crearts.activebreak.ACTION_POSTPONE"
    const val ACTION_TOGGLE_STATUS = "xyz.crearts.activebreak.ACTION_TOGGLE_STATUS"
    const val EXTRA_ACTIVITY_TITLE = "activity_title"
    const val EXTRA_ACTIVITY_DESCRIPTION = "activity_description"
    const val EXTRA_IS_TODO = "is_todo"

    // Constants for notification click handling in MainActivity
    const val EXTRA_FROM_NOTIFICATION = "from_notification"
    const val EXTRA_NOTIFICATION_TYPE = "notification_type"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for break reminders
            val reminderChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Напоминания о перерывах и активности"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel for persistent status notification
            val statusChannel = NotificationChannel(STATUS_CHANNEL_ID, STATUS_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "Постоянное уведомление о статусе ActiveBreak"
                enableVibration(false)
                setShowBadge(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    fun showBreakNotification(context: Context, activity: BreakActivity) {
        createNotificationChannel(context)
        showNotification(
            context = context,
            title = "Время для перерыва! 🚀",
            contentText = activity.title,
            description = activity.description ?: "Сделайте небольшой перерыв для здоровья!",
            notificationId = NOTIFICATION_ID,
            extraTitle = activity.title,
            extraDescription = activity.description
        )
    }

    fun showTodoNotification(context: Context, task: TodoTask) {
        createNotificationChannel(context)
        showNotification(
            context = context,
            title = "Напоминание! ✅",
            contentText = task.title,
            description = task.description ?: "Время выполнить задачу!",
            notificationId = TODO_NOTIFICATION_ID,
            extraTitle = task.title,
            extraDescription = task.description,
            isTodo = true
        )
    }

    private fun showNotification(
        context: Context,
        title: String,
        contentText: String,
        description: String,
        notificationId: Int,
        extraTitle: String,
        extraDescription: String?,
        isTodo: Boolean = false
    ) {
        // Intent для открытия приложения с данными о нотификации
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_ACTIVITY_TITLE, extraTitle)
            putExtra(EXTRA_ACTIVITY_DESCRIPTION, extraDescription)
            putExtra(EXTRA_IS_TODO, isTodo)
            putExtra(EXTRA_NOTIFICATION_TYPE, if (isTodo) "TODO" else "BREAK")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // PendingIntent для кнопки "Выполнено"
        val completedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COMPLETED
            putExtra(EXTRA_ACTIVITY_TITLE, extraTitle)
            putExtra(EXTRA_IS_TODO, isTodo)
        }
        val completedPendingIntent = PendingIntent.getBroadcast(
            context, notificationId * 10 + 1, completedIntent, // Уникальный request code
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // PendingIntent для кнопки "Отложить"
        val postponeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_POSTPONE
            putExtra(EXTRA_ACTIVITY_TITLE, extraTitle)
            putExtra(EXTRA_IS_TODO, isTodo)
        }
        val postponePendingIntent = PendingIntent.getBroadcast(
            context, notificationId * 10 + 2, postponeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )



        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\n\n$description"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_send, "Выполнено", completedPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Отложить", postponePendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        }
    }

    fun dismissNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.cancel(TODO_NOTIFICATION_ID)
    }

    /**
     * Show persistent status notification in notification shade
     */
    fun showStatusNotification(context: Context, isActive: Boolean) {
        createNotificationChannel(context)

        // Intent to open the app when notification is clicked
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent for toggle action
        val toggleIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_STATUS
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context, STATUS_NOTIFICATION_ID * 10 + 1, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val statusText = if (isActive) "Активен" else "На паузе"
        val actionText = if (isActive) "Пауза" else "Возобновить"
        val icon = if (isActive) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val notification = NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            //.setContentTitle("Перерывы")
            .setContentText(statusText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Напоминания о перерывах: $statusText\nНажмите для управления"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true) // Makes notification persistent
            .setShowWhen(false)
            .addAction(icon, actionText, togglePendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
        }
    }

    /**
     * Hide persistent status notification
     */
    fun hideStatusNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(STATUS_NOTIFICATION_ID)
    }

    /**
     * Check if status notification is currently shown
     */
    fun isStatusNotificationShown(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.any { it.id == STATUS_NOTIFICATION_ID }
        } else {
            // For older versions, we can't easily check, so assume false
            false
        }
    }
}
