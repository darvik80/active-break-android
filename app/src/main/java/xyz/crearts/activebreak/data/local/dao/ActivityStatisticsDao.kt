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
}
