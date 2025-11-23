package xyz.crearts.activebreak.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.workers.BreakReminderWorker
import xyz.crearts.activebreak.workers.NotificationHelper
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val activityTitle = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_TITLE) ?: return
        val activityDescription = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_DESCRIPTION)
        val isTodo = intent.getBooleanExtra(NotificationHelper.EXTRA_IS_TODO, false)

        when (intent.action) {
            NotificationHelper.ACTION_COMPLETED -> {
                // Закрываем уведомление
                NotificationHelper.dismissNotification(context)

                // Сохраняем статистику
                saveActivityStatistics(context, activityTitle, isTodo)

                Toast.makeText(context, "Отлично! Продолжайте в том же духе! 💪", Toast.LENGTH_SHORT).show()
            }

            NotificationHelper.ACTION_POSTPONE -> {
                // Закрываем уведомление
                NotificationHelper.dismissNotification(context)

                // Перепланируем напоминание через 10 минут
                // TODO: Для TODO задач нужно использовать отдельный Worker или передавать параметры
                if (!isTodo) {
                    val workRequest = OneTimeWorkRequestBuilder<BreakReminderWorker>()
                        .setInitialDelay(10, TimeUnit.MINUTES)
                        .build()

                    WorkManager.getInstance(context).enqueue(workRequest)
                } else {
                     // Логика для откладывания TODO (можно реализовать позже)
                }

                Toast.makeText(context, "Напомним через 10 минут ⏰", Toast.LENGTH_SHORT).show()
            }

            NotificationHelper.ACTION_SHARE -> {
                // Открываем шеринг
                shareActivity(context, activityTitle, activityDescription, isTodo)
            }
        }
    }

    private fun shareActivity(context: Context, title: String, description: String?, isTodo: Boolean) {
        val shareText = buildString {
            if (isTodo) {
                append("Моя задача выполнена: $title ✅")
            } else {
                append("Мое задание на перерыв: $title 💪")
            }
            
            if (!description.isNullOrBlank()) {
                append("\n\n$description")
            }
            append("\n\n#ActiveBreak #ЗдоровыйОбразЖизни")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, if (isTodo) "ActiveBreak - Моя задача" else "ActiveBreak - Мой перерыв")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Поделиться через").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }

    private fun saveActivityStatistics(context: Context, activityTitle: String, isTodo: Boolean) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(context)
            database.activityStatisticsDao().insert(
                xyz.crearts.activebreak.data.local.entity.ActivityStatistics(
                    activityTitle = activityTitle,
                    activityType = if (isTodo) "TODO" else "BREAK"
                )
            )
            
            // Если это TODO, помечаем саму задачу как выполненную (нужна логика поиска задачи по названию или передача ID)
            // Пока просто сохраняем статистику
        }
    }
}
