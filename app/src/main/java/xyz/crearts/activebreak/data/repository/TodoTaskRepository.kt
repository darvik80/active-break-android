package xyz.crearts.activebreak.data.repository

import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.dao.TodoTaskDao
import xyz.crearts.activebreak.data.local.entity.TodoTask

class TodoTaskRepository(private val dao: TodoTaskDao) {
    val allTasks: Flow<List<TodoTask>> = dao.getAllTasks()
    val activeTasks: Flow<List<TodoTask>> = dao.getActiveTasks()

    suspend fun insert(task: TodoTask) = dao.insert(task)
    suspend fun update(task: TodoTask) = dao.update(task)
    suspend fun delete(task: TodoTask) = dao.delete(task)

    suspend fun toggleTaskCompletion(task: TodoTask) {
        val updatedTask = if (task.isCompleted) {
            // Mark as incomplete, clear completion time
            task.copy(isCompleted = false, completedAt = null)
        } else {
            // Mark as complete, set completion time
            task.copy(isCompleted = true, completedAt = System.currentTimeMillis())
        }
        update(updatedTask)
    }

    suspend fun toggleTaskPause(task: TodoTask) {
        update(task.copy(isPaused = !task.isPaused))
    }
}
