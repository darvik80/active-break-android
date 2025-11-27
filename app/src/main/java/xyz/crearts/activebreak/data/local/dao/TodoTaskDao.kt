package xyz.crearts.activebreak.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.entity.TodoTask

@Dao
interface TodoTaskDao {
    @Query("""
        SELECT * FROM todo_tasks 
        WHERE (is_completed = 0) 
           OR (is_completed = 1 AND completed_at IS NOT NULL AND completed_at > :weekAgoTimestamp)
           OR (is_completed = 1 AND completed_at IS NULL)
        ORDER BY 
            CASE WHEN is_completed = 0 THEN 0 ELSE 1 END,
            CASE WHEN is_completed = 0 THEN 
                CASE WHEN due_date IS NULL THEN 1 ELSE 0 END
            ELSE 0 END,
            CASE WHEN is_completed = 0 THEN due_date ELSE completed_at END DESC
    """)
    fun getAllTasks(weekAgoTimestamp: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000): Flow<List<TodoTask>>

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
