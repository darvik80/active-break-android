package xyz.crearts.activebreak.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.entity.ActivityStatistics

@Dao
interface ActivityStatisticsDao {
    @Query("SELECT * FROM activity_statistics ORDER BY completed_at DESC")
    fun getAllStatistics(): Flow<List<ActivityStatistics>>

    @Query("SELECT * FROM activity_statistics WHERE completed_at >= :startDate ORDER BY completed_at DESC")
    fun getStatisticsSince(startDate: Long): Flow<List<ActivityStatistics>>

    @Query("SELECT COUNT(*) FROM activity_statistics WHERE completed_at >= :startDate AND activity_type = :type")
    suspend fun getCompletedCountSince(startDate: Long, type: String): Int

    @Query("SELECT COUNT(*) FROM activity_statistics WHERE DATE(completed_at/1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')")
    fun getTodayCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM activity_statistics WHERE DATE(completed_at/1000, 'unixepoch', 'localtime') >= DATE('now', '-7 days', 'localtime')")
    fun getWeekCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statistics: ActivityStatistics): Long

    @Query("DELETE FROM activity_statistics WHERE completed_at < :beforeDate")
    suspend fun deleteOldStatistics(beforeDate: Long)

    // Chart data queries
    @Query("""
        SELECT 
            CASE strftime('%w', completed_at/1000, 'unixepoch', 'localtime')
                WHEN '0' THEN 'Вс'
                WHEN '1' THEN 'Пн'
                WHEN '2' THEN 'Вт'
                WHEN '3' THEN 'Ср'
                WHEN '4' THEN 'Чт'
                WHEN '5' THEN 'Пт'
                WHEN '6' THEN 'Сб'
            END as day_name,
            COUNT(*) as count
        FROM activity_statistics 
        WHERE completed_at >= :weekAgoTimestamp
        GROUP BY strftime('%w', completed_at/1000, 'unixepoch', 'localtime')
        ORDER BY strftime('%w', completed_at/1000, 'unixepoch', 'localtime')
    """)
    suspend fun getWeeklyActivityData(weekAgoTimestamp: Long): List<DayActivityCount>

    @Query("""
        SELECT 
            activity_type,
            COUNT(*) as count
        FROM activity_statistics 
        WHERE completed_at >= :weekAgoTimestamp
        GROUP BY activity_type
    """)
    suspend fun getActivityTypeData(weekAgoTimestamp: Long): List<ActivityTypeCount>
}

data class DayActivityCount(
    val day_name: String,
    val count: Int
)

data class ActivityTypeCount(
    val activity_type: String,
    val count: Int
)
