package xyz.crearts.activebreak.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.entity.BreakActivity

@Dao
interface BreakActivityDao {
    @Query("SELECT * FROM break_activities WHERE is_active = 1")
    fun getAllActiveActivities(): Flow<List<BreakActivity>>

    @Query("SELECT * FROM break_activities WHERE is_active = 1 AND (time_of_day = :timeOfDay OR time_of_day = 'ANY')")
    suspend fun getActivitiesByTimeOfDay(timeOfDay: String): List<BreakActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: BreakActivity): Long

    @Update
    suspend fun update(activity: BreakActivity)

    @Delete
    suspend fun delete(activity: BreakActivity)

    @Query("SELECT * FROM break_activities ORDER BY created_at DESC")
    fun getAllActivities(): Flow<List<BreakActivity>>

    @Query("SELECT COUNT(*) FROM break_activities")
    suspend fun getCount(): Int
}
