package xyz.crearts.activebreak.ui.screens.settings

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.preferences.Settings
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.workers.BreakReminderWorker
import xyz.crearts.activebreak.workers.MessengerHelper

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager.instance

    val settings: StateFlow<Settings> = settingsManager.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun updateStartTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsManager.updateSettings(
                currentSettings.copy(startHour = hour, startMinute = minute)
            )
        }
    }

    fun updateEndTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsManager.updateSettings(
                currentSettings.copy(endHour = hour, endMinute = minute)
            )
        }
    }

    fun updateInterval(intervalMinutes: Long) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsManager.updateSettings(
                currentSettings.copy(intervalMinutes = intervalMinutes)
            )

            // Перепланируем работу с новым интервалом
            if (currentSettings.isEnabled) {
                BreakReminderWorker.scheduleWork(getApplication(), intervalMinutes)
            }
        }
    }

    fun updateSettings(newSettings: xyz.crearts.activebreak.data.preferences.Settings) {
        viewModelScope.launch {
            settingsManager.updateSettings(newSettings)
        }
    }

    fun testBreakNotification() {
        viewModelScope.launch {
            val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(getApplication())
            val repository = xyz.crearts.activebreak.data.repository.BreakActivityRepository(database.breakActivityDao())
            val activity = repository.getRandomActivity()

            activity?.let {
                xyz.crearts.activebreak.workers.NotificationHelper.showBreakNotification(
                    getApplication(),
                    it
                )
            }
        }
    }

    fun testTodoNotification() {
        viewModelScope.launch {
            val database = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(getApplication())
            val todoDao = database.todoTaskDao()
            val tasks = todoDao.getAllTasks().first()

            tasks.firstOrNull()?.let { task ->
                xyz.crearts.activebreak.workers.TodoReminderWorker.scheduleTodoReminder(
                    getApplication(),
                    task
                )
            }
        }
    }
    
    fun testTelegramIntegration(token: String, chatId: String) {
        viewModelScope.launch {
            if (token.isBlank() || chatId.isBlank()) {
                Toast.makeText(getApplication(), "Токен или Chat ID не заполнены", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val success = MessengerHelper.sendToTelegram(
                token,
                chatId,
                "🔔 Тестовое сообщение от ActiveBreak!\nИнтеграция работает успешно. 🚀"
            )
            
            if (success) {
                Toast.makeText(getApplication(), "Сообщение отправлено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "Ошибка отправки. Проверьте токен и Chat ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun testWhatsAppIntegration(number: String) {
        if (number.isBlank()) {
            Toast.makeText(getApplication(), "Номер телефона не заполнен", Toast.LENGTH_SHORT).show()
            return
        }
        
        MessengerHelper.sendToWhatsApp(
            getApplication(),
            number,
            "🔔 Тестовое сообщение от ActiveBreak! Интеграция работает."
        )
    }
}
