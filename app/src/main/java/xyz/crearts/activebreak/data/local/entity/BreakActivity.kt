package xyz.crearts.activebreak.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "break_activities")
data class BreakActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "weight")
    val weight: Int = 1, // Вес для выбора активности

    @ColumnInfo(name = "time_of_day")
    val timeOfDay: String = "ANY", // MORNING, AFTERNOON, EVENING, ANY

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
