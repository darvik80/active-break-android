package xyz.crearts.activebreak.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class TodoCategory(
    val displayName: String,
    val icon: ImageVector,
    val emoji: String
) {
    SPORT("Спорт", Icons.Default.FitnessCenter, "🏃"),
    WALK("Прогулка", Icons.AutoMirrored.Filled.DirectionsWalk, "🚶"),
    BIKE("Велосипед", Icons.AutoMirrored.Filled.DirectionsBike, "🚴"),
    SHOPPING("Покупки", Icons.Default.ShoppingCart, "🛒"),
    HEALTH("Здоровье", Icons.Default.Favorite, "❤️"),
    RELAX("Отдых", Icons.Default.Spa, "🧘"),
    SAUNA("Сауна/Баня", Icons.Default.HotTub, "🧖"),
    WORK("Работа", Icons.Default.Work, "💼"),
    STUDY("Учёба", Icons.Default.School, "📚"),
    HOME("Дом", Icons.Default.Home, "🏠"),
    FOOD("Еда", Icons.Default.Restaurant, "🍽️"),
    TRAVEL("Путешествие", Icons.Default.Flight, "✈️"),
    OTHER("Другое", Icons.AutoMirrored.Filled.Assignment, "📋");

    companion object {
        fun fromString(value: String): TodoCategory {
            return values().find { it.name == value } ?: OTHER
        }
    }
}
