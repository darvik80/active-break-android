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
            val task = TodoTask(
                title = title,
                description = description,
                dueDate = dueDate,
                category = category,
                recurrenceType = recurrenceType,
                recurrenceDays = recurrenceDays,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore,
                nextDueDate = dueDate
            )

            val taskId = repository.insert(task)

            // Запланировать напоминание если включено
            if (reminderEnabled && dueDate != null) {
                xyz.crearts.activebreak.workers.TodoReminderWorker.scheduleTodoReminder(
                    getApplication(),
                    task.copy(id = taskId)
                )
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
