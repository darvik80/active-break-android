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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TodoTask): Long

    @Update
    suspend fun update(task: TodoTask)

    @Delete
    suspend fun delete(task: TodoTask)
}
