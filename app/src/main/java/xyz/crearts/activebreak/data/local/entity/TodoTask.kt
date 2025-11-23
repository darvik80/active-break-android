package xyz.crearts.activebreak.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_tasks")
data class TodoTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "is_paused")
    val isPaused: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    // Категория/Иконка
    @ColumnInfo(name = "category")
    val category: String = "OTHER", // SPORT, WALK, SHOPPING, RELAX, HEALTH, WORK, STUDY, HOME, OTHER

    // Периодичность
    @ColumnInfo(name = "recurrence_type")
    val recurrenceType: String = "NONE", // NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY, CUSTOM_DAYS

    @ColumnInfo(name = "recurrence_days")
    val recurrenceDays: String? = null, // Для CUSTOM_DAYS: "1,3,5" (пн, ср, пт)

    // Напоминание
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,

    @ColumnInfo(name = "reminder_minutes_before")
    val reminderMinutesBefore: Int = 15, // 5, 15, 30, 60, 120, 1440 (день)

    @ColumnInfo(name = "last_completed_date")
    val lastCompletedDate: Long? = null,

    @ColumnInfo(name = "next_due_date")
    val nextDueDate: Long? = null
)
