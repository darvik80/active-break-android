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
import xyz.crearts.activebreak.data.local.dao.ActivityTypeCount
import xyz.crearts.activebreak.data.local.dao.DayActivityCount
import xyz.crearts.activebreak.data.preferences.Settings
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.data.repository.BreakActivityRepository
import xyz.crearts.activebreak.ui.components.charts.ChartData
import xyz.crearts.activebreak.ui.components.charts.PieChartData
import xyz.crearts.activebreak.workers.BreakReminderWorker
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // Use dependency injection pattern instead of direct database creation
    private val settingsManager = SettingsManager.instance

    // Lazy initialization to avoid blocking main thread
    private val database by lazy { AppDatabase.getDatabase(application) }
    private val repository by lazy { BreakActivityRepository(database.breakActivityDao()) }
    private val statisticsDao by lazy { database.activityStatisticsDao() }

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

    // Chart data methods
    suspend fun getWeeklyChartData(): List<ChartData> {
        return try {
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val rawData = statisticsDao.getWeeklyActivityData(weekAgo)

            // Ensure all days are present with 0 count if no data
            val allDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            allDays.map { day ->
                val count = rawData.find { it.day_name == day }?.count ?: 0
                ChartData(label = day, value = count.toFloat())
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error getting weekly chart data: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getActivityTypePieData(): List<PieChartData> {
        return try {
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val rawData = statisticsDao.getActivityTypeData(weekAgo)

            rawData.map { data ->
                val label = when (data.activity_type) {
                    "BREAK" -> "Перерывы"
                    "TODO" -> "Задачи"
                    else -> data.activity_type
                }
                val color = when (data.activity_type) {
                    "BREAK" -> Color(0xFF4CAF50) // Green
                    "TODO" -> Color(0xFF2196F3)  // Blue
                    else -> Color(0xFF9E9E9E)    // Gray
                }
                PieChartData(
                    label = label,
                    value = data.count.toFloat(),
                    color = color
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error getting activity type pie data: ${e.message}", e)
            emptyList()
        }
    }



    fun updateInterval(intervalMinutes: Long) {
        viewModelScope.launch {
            try {
                val currentSettings = settings.value
                settingsManager.updateSettings(currentSettings.copy(intervalMinutes = intervalMinutes))

                if (currentSettings.isEnabled) {
                    BreakReminderWorker.scheduleWork(getApplication(), intervalMinutes)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error updating interval: ${e.message}", e)
            }
        }
    }

    fun testNotification() {
        viewModelScope.launch {
            try {
                val activity = repository.getRandomActivity()
                activity?.let {
                    xyz.crearts.activebreak.workers.NotificationHelper.showBreakNotification(
                        getApplication(),
                        it
                    )
                } ?: run {
                    android.util.Log.w("HomeViewModel", "No activities available for test notification")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error showing test notification: ${e.message}", e)
            }
        }
    }

    fun checkWorkManagerStatus(): Boolean {
        return try {
            val workManager = androidx.work.WorkManager.getInstance(getApplication())
            val workInfos = workManager.getWorkInfosForUniqueWork(
                xyz.crearts.activebreak.workers.BreakReminderWorker.WORK_NAME
            ).get()

            workInfos.any {
                it.state == androidx.work.WorkInfo.State.ENQUEUED ||
                it.state == androidx.work.WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error checking WorkManager status: ${e.message}", e)
            false
        }
    }
}
