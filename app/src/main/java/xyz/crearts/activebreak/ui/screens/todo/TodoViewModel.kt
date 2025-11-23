package xyz.crearts.activebreak.ui.screens.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.local.AppDatabase
import xyz.crearts.activebreak.data.local.entity.TodoTask
import xyz.crearts.activebreak.data.repository.TodoTaskRepository
import java.util.Calendar

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TodoTaskRepository(database.todoTaskDao())

    val tasks = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(
        title: String, 
        description: String?, 
        dueDate: Long?,
        hour: Int,
        minute: Int,
        recurrenceType: String,
        recurrenceDays: String?,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int,
        category: String
    ) {
        viewModelScope.launch {
            val finalDueDate = dueDate?.let { date ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = date
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }

            val task = TodoTask(
                title = title,
                description = description,
                dueDate = finalDueDate,
                category = category,
                recurrenceType = recurrenceType,
                recurrenceDays = recurrenceDays,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore,
                nextDueDate = finalDueDate
            )

            val taskId = repository.insert(task)

            // Запланировать напоминание если включено
            if (reminderEnabled && finalDueDate != null) {
                // Рассчитываем время напоминания
                val reminderTime = finalDueDate - (reminderMinutesBefore * 60 * 1000)
                if (reminderTime > System.currentTimeMillis()) {
                    xyz.crearts.activebreak.workers.TodoReminderWorker.scheduleTodoReminder(
                        getApplication(),
                        task.copy(id = taskId)
                    )
                }
            }
        }
    }

    fun updateTask(task: TodoTask) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun toggleTaskCompletion(task: TodoTask) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun toggleTaskPause(task: TodoTask) {
        viewModelScope.launch {
            repository.toggleTaskPause(task)
        }
    }
}
