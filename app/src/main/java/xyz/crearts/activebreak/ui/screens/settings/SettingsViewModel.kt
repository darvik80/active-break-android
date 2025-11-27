package xyz.crearts.activebreak.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.preferences.Settings
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.workers.BreakReminderWorker
import xyz.crearts.activebreak.workers.MessengerHelper

// UI Events for proper MVVM architecture
sealed class SettingsUiEvent {
    data class ShowMessage(val message: String) : SettingsUiEvent()
    data class ShowError(val error: String) : SettingsUiEvent()
    object ShowRestartDialog : SettingsUiEvent()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager.instance

    val settings: StateFlow<Settings> = settingsManager.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    // UI Events channel for proper MVVM communication
    private val _uiEvents = Channel<SettingsUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    // Loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private fun sendEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            _uiEvents.send(event)
        }
    }

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

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = settings.value
                settingsManager.updateSettings(currentSettings.copy(isEnabled = enabled))

                if (enabled) {
                    BreakReminderWorker.scheduleWork(
                        getApplication(),
                        currentSettings.intervalMinutes
                    )
                } else {
                    BreakReminderWorker.cancelWork(getApplication())
                }
            } catch (e: Exception) {
                // Log error but don't crash the app
                android.util.Log.e("SettingsViewModel", "Error toggling enabled state: ${e.message}", e)
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            val currentSettings = settings.value

            // Check if language actually changed
            val languageChanged = currentSettings.language != languageCode

            if (languageChanged) {
                settingsManager.updateSettings(
                    currentSettings.copy(language = languageCode)
                )
                // Apply locale change immediately
                xyz.crearts.activebreak.utils.LocaleHelper.setLocale(languageCode)
                // Show restart dialog only if language changed
                sendEvent(SettingsUiEvent.ShowRestartDialog)
            }
        }
    }

    fun testBreakNotification() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val repository = xyz.crearts.activebreak.data.repository.BreakActivityRepository(
                    xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(getApplication()).breakActivityDao()
                )
                val activity = repository.getRandomActivity()

                if (activity != null) {
                    xyz.crearts.activebreak.workers.NotificationHelper.showBreakNotification(getApplication(), activity)
                    sendEvent(SettingsUiEvent.ShowMessage("Тестовое уведомление отправлено!"))
                } else {
                    sendEvent(SettingsUiEvent.ShowError("Нет доступных активностей для тестирования"))
                }
            } catch (e: Exception) {
                sendEvent(SettingsUiEvent.ShowError("Ошибка при отправке уведомления: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun testTodoNotification() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val todoDao = xyz.crearts.activebreak.data.local.AppDatabase.getDatabase(getApplication()).todoTaskDao()

                // Get existing task or create new one - optimized approach
                val testTask = todoDao.getFirstActiveTask()
                    ?: todoDao.getAnyTask()
                    ?: createTestTask(todoDao)

                xyz.crearts.activebreak.workers.TodoReminderWorker.scheduleTodoReminder(getApplication(), testTask)
                sendEvent(SettingsUiEvent.ShowMessage("Тестовое напоминание о задаче запланировано!"))
            } catch (e: Exception) {
                sendEvent(SettingsUiEvent.ShowError("Ошибка при создании напоминания: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createTestTask(todoDao: xyz.crearts.activebreak.data.local.dao.TodoTaskDao): xyz.crearts.activebreak.data.local.entity.TodoTask {
        val testTask = xyz.crearts.activebreak.data.local.entity.TodoTask(
            title = "Тестовая задача",
            description = "Это тестовое напоминание для проверки уведомлений",
            category = "OTHER",
            reminderEnabled = true
        )
        val taskId = todoDao.insert(testTask)
        return testTask.copy(id = taskId)
    }
    
    fun testTelegramIntegration(token: String, chatId: String) {
        if (token.isBlank() || chatId.isBlank()) {
            sendEvent(SettingsUiEvent.ShowError("Токен или Chat ID не заполнены"))
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = MessengerHelper.sendToTelegram(token, chatId,
                    "🔔 Тестовое сообщение от ActiveBreak!\nИнтеграция работает успешно. 🚀")

                val message = if (success) "Сообщение отправлено!" else "Ошибка отправки. Проверьте токен и Chat ID"
                sendEvent(if (success) SettingsUiEvent.ShowMessage(message) else SettingsUiEvent.ShowError(message))
            } catch (e: Exception) {
                sendEvent(SettingsUiEvent.ShowError("Ошибка при отправке в Telegram: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetFirstLaunchFlag() {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsManager.updateSettings(
                currentSettings.copy(isFirstLaunch = true)
            )
            sendEvent(SettingsUiEvent.ShowMessage("Флаг первого запуска сброшен. Перезапустите приложение для тестирования."))
        }
    }
}
