package xyz.crearts.activebreak.workers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object MessengerHelper {

    /**
     * Отправка сообщения в Telegram через Bot API
     */
    suspend fun sendToTelegram(
        botToken: String,
        chatId: String,
        message: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.telegram.org/bot$botToken/sendMessage"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Экранируем специальные символы для JSON
            val safeMessage = message.replace("\"", "\\\"").replace("\n", "\\n")

            val jsonPayload = """
                {
                    "chat_id": "$chatId",
                    "text": "$safeMessage",
                    "parse_mode": "HTML"
                }
            """.trimIndent()

            connection.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode


            if (responseCode != 200) {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("MessengerHelper", "Telegram error: $errorStream")
            }

            responseCode == 200
        } catch (e: Exception) {
            Log.e("MessengerHelper", "Error sending to Telegram: ${e.message}", e)
            false
        }
    }

    /**
     * Форматирование сообщения для уведомления о перерыве
     */
    fun formatBreakMessage(activityTitle: String, activityDescription: String?): String {
        return buildString {
            append("⏰ <b>Время для перерыва!</b>\n\n")
            append("📋 $activityTitle\n")
            if (!activityDescription.isNullOrBlank()) {
                append("\n$activityDescription")
            }
            append("\n\n💪 Заботься о своём здоровье!")
        }
    }

    /**
     * Форматирование сообщения для уведомления о задаче
     */
    fun formatTodoMessage(taskTitle: String, taskDescription: String?): String {
        return buildString {
            append("✅ <b>Напоминание о задаче!</b>\n\n")
            append("📌 $taskTitle\n")
            if (!taskDescription.isNullOrBlank()) {
                append("\n$taskDescription")
            }
            append("\n\n🎯 Не забудь выполнить!")
        }
    }
}
