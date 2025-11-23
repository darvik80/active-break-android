package xyz.crearts.activebreak.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.crearts.activebreak.ActiveBreakApplication

data class Settings(
    val isEnabled: Boolean = true, // Master switch
    val breakNotificationsEnabled: Boolean = true,
    val todoNotificationsEnabled: Boolean = true,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 18,
    val endMinute: Int = 0,
    val intervalMinutes: Long = 30,
    val telegramEnabled: Boolean = false,
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val whatsappEnabled: Boolean = false,
    val whatsappNumber: String = ""
)

class SettingsManager(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val BREAK_NOTIFICATIONS_ENABLED = booleanPreferencesKey("break_notifications_enabled")
        val TODO_NOTIFICATIONS_ENABLED = booleanPreferencesKey("todo_notifications_enabled")
        val START_HOUR = intPreferencesKey("start_hour")
        val START_MINUTE = intPreferencesKey("start_minute")
        val END_HOUR = intPreferencesKey("end_hour")
        val END_MINUTE = intPreferencesKey("end_minute")
        val INTERVAL_MINUTES = longPreferencesKey("interval_minutes")
        val TELEGRAM_ENABLED = booleanPreferencesKey("telegram_enabled")
        val TELEGRAM_BOT_TOKEN = stringPreferencesKey("telegram_bot_token")
        val TELEGRAM_CHAT_ID = stringPreferencesKey("telegram_chat_id")
        val WHATSAPP_ENABLED = booleanPreferencesKey("whatsapp_enabled")
        val WHATSAPP_NUMBER = stringPreferencesKey("whatsapp_number")
    }

    fun getSettings(): Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
            breakNotificationsEnabled = preferences[PreferencesKeys.BREAK_NOTIFICATIONS_ENABLED] ?: true,
            todoNotificationsEnabled = preferences[PreferencesKeys.TODO_NOTIFICATIONS_ENABLED] ?: true,
            startHour = preferences[PreferencesKeys.START_HOUR] ?: 9,
            startMinute = preferences[PreferencesKeys.START_MINUTE] ?: 0,
            endHour = preferences[PreferencesKeys.END_HOUR] ?: 18,
            endMinute = preferences[PreferencesKeys.END_MINUTE] ?: 0,
            intervalMinutes = preferences[PreferencesKeys.INTERVAL_MINUTES] ?: 30,
            telegramEnabled = preferences[PreferencesKeys.TELEGRAM_ENABLED] ?: false,
            telegramBotToken = preferences[PreferencesKeys.TELEGRAM_BOT_TOKEN] ?: "",
            telegramChatId = preferences[PreferencesKeys.TELEGRAM_CHAT_ID] ?: "",
            whatsappEnabled = preferences[PreferencesKeys.WHATSAPP_ENABLED] ?: false,
            whatsappNumber = preferences[PreferencesKeys.WHATSAPP_NUMBER] ?: ""
        )
    }

    suspend fun updateSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ENABLED] = settings.isEnabled
            preferences[PreferencesKeys.BREAK_NOTIFICATIONS_ENABLED] = settings.breakNotificationsEnabled
            preferences[PreferencesKeys.TODO_NOTIFICATIONS_ENABLED] = settings.todoNotificationsEnabled
            preferences[PreferencesKeys.START_HOUR] = settings.startHour
            preferences[PreferencesKeys.START_MINUTE] = settings.startMinute
            preferences[PreferencesKeys.END_HOUR] = settings.endHour
            preferences[PreferencesKeys.END_MINUTE] = settings.endMinute
            preferences[PreferencesKeys.INTERVAL_MINUTES] = settings.intervalMinutes
            preferences[PreferencesKeys.TELEGRAM_ENABLED] = settings.telegramEnabled
            preferences[PreferencesKeys.TELEGRAM_BOT_TOKEN] = settings.telegramBotToken
            preferences[PreferencesKeys.TELEGRAM_CHAT_ID] = settings.telegramChatId
            preferences[PreferencesKeys.WHATSAPP_ENABLED] = settings.whatsappEnabled
            preferences[PreferencesKeys.WHATSAPP_NUMBER] = settings.whatsappNumber
        }
    }

    companion object {
        // Singleton instance
        val instance: SettingsManager by lazy {
            SettingsManager(ActiveBreakApplication.instance.dataStore)
        }
    }
}
