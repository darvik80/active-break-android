package xyz.crearts.activebreak.ui.screens.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.local.AppDatabase
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import xyz.crearts.activebreak.data.repository.BreakActivityRepository

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BreakActivityRepository(database.breakActivityDao())

    val activities = repository.allActivities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addActivity(title: String, description: String?, timeOfDay: String, weight: Int) {
        viewModelScope.launch {
            repository.insert(
                BreakActivity(
                    title = title,
                    description = description,
                    timeOfDay = timeOfDay,
                    weight = weight
                )
            )
        }
    }

    fun updateActivity(activity: BreakActivity) {
        viewModelScope.launch {
            repository.update(activity)
        }
    }

    fun deleteActivity(activity: BreakActivity) {
        viewModelScope.launch {
            repository.delete(activity)
        }
    }

    fun toggleActivityActive(activity: BreakActivity) {
        viewModelScope.launch {
            repository.update(activity.copy(isActive = !activity.isActive))
        }
    }
}
