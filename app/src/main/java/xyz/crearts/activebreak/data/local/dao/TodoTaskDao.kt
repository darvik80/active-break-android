package xyz.crearts.activebreak.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.entity.TodoTask

@Dao
interface TodoTaskDao {
    @Query("SELECT * FROM todo_tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE is_completed = 0 AND is_paused = 0 ORDER BY due_date ASC")
    fun getActiveTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TodoTask?

    @Query("SELECT COUNT(*) FROM todo_tasks")
    suspend fun getCount(): Int

    // Optimized methods to get only one record instead of loading all tasks
    @Query("SELECT * FROM todo_tasks WHERE is_completed = 0 AND is_paused = 0 ORDER BY due_date ASC LIMIT 1")
    suspend fun getFirstActiveTask(): TodoTask?

    @Query("SELECT * FROM todo_tasks ORDER BY created_at DESC LIMIT 1")
    suspend fun getAnyTask(): TodoTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TodoTask): Long

    @Update
    suspend fun update(task: TodoTask)

    @Delete
    suspend fun delete(task: TodoTask)
}
