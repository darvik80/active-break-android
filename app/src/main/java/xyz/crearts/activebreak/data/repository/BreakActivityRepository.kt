package xyz.crearts.activebreak.data.repository

import kotlinx.coroutines.flow.Flow
import xyz.crearts.activebreak.data.local.dao.BreakActivityDao
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import java.util.Calendar

class BreakActivityRepository(private val dao: BreakActivityDao) {
    val allActivities: Flow<List<BreakActivity>> = dao.getAllActivities()
    val activeActivities: Flow<List<BreakActivity>> = dao.getAllActiveActivities()

    suspend fun getRandomActivity(): BreakActivity? {
        val timeOfDay = getCurrentTimeOfDay()
        val activities = dao.getActivitiesByTimeOfDay(timeOfDay)

        if (activities.isEmpty()) return null

        // Weighted random selection
        val totalWeight = activities.sumOf { it.weight }
        var random = (0 until totalWeight).random()

        for (activity in activities) {
            random -= activity.weight
            if (random < 0) return activity
        }

        return activities.firstOrNull()
    }

    private fun getCurrentTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..9 -> "EARLY_MORNING"    // 6-9: Энергичное утро
            in 10..11 -> "LATE_MORNING"   // 10-11: Позднее утро
            in 12..14 -> "MIDDAY"         // 12-14: Обед
            in 15..17 -> "AFTERNOON"      // 15-17: День
            in 18..20 -> "EVENING"        // 18-20: Вечер
            in 21..23 -> "LATE_EVENING"   // 21-23: Поздний вечер
            else -> "ANY"                 // Ночь/ANY
        }
    }

    suspend fun insert(activity: BreakActivity) = dao.insert(activity)
    suspend fun update(activity: BreakActivity) = dao.update(activity)
    suspend fun delete(activity: BreakActivity) = dao.delete(activity)
}
