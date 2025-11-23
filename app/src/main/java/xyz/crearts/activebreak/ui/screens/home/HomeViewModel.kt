package xyz.crearts.activebreak.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.local.AppDatabase
import xyz.crearts.activebreak.data.preferences.Settings
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.data.repository.BreakActivityRepository
import xyz.crearts.activebreak.workers.BreakReminderWorker

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BreakActivityRepository(database.breakActivityDao())
    private val settingsManager = SettingsManager.instance
    private val statisticsDao = database.activityStatisticsDao()

    val settings: StateFlow<Settings> = settingsManager.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    val activities = repository.activeActivities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayCompletedCount = statisticsDao.getTodayCompletedCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val weekCompletedCount = statisticsDao.getWeekCompletedCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
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
        }
    }

    fun updateInterval(intervalMinutes: Long) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsManager.updateSettings(currentSettings.copy(intervalMinutes = intervalMinutes))

            if (currentSettings.isEnabled) {
                BreakReminderWorker.scheduleWork(getApplication(), intervalMinutes)
            }
        }
    }

    fun testNotification() {
        viewModelScope.launch {
            val activity = repository.getRandomActivity()
            activity?.let {
                xyz.crearts.activebreak.workers.NotificationHelper.showBreakNotification(
                    getApplication(),
                    it
                )
            }
        }
    }

    fun checkWorkManagerStatus(): Boolean {
        val workManager = androidx.work.WorkManager.getInstance(getApplication())
        val workInfos = workManager.getWorkInfosForUniqueWork(
            xyz.crearts.activebreak.workers.BreakReminderWorker.WORK_NAME
        ).get()

        return workInfos.any { 
            it.state == androidx.work.WorkInfo.State.ENQUEUED || 
            it.state == androidx.work.WorkInfo.State.RUNNING 
        }
    }
}
