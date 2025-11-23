package xyz.crearts.activebreak.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_statistics")
data class ActivityStatistics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "activity_title")
    val activityTitle: String,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "activity_type")
    val activityType: String = "BREAK" // BREAK или TODO
)
