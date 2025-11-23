package xyz.crearts.activebreak.domain.model

enum class RecurrenceType(val displayName: String) {
    NONE("Разовая"),
    DAILY("Ежедневно"),
    WEEKLY("Еженедельно"),
    BIWEEKLY("Раз в 2 недели"),
    MONTHLY("Ежемесячно"),
    CUSTOM_DAYS("По дням недели")
}

enum class ReminderTime(val displayName: String, val minutes: Int) {
    MIN_5("За 5 минут", 5),
    MIN_15("За 15 минут", 15),
    MIN_30("За 30 минут", 30),
    HOUR_1("За 1 час", 60),
    HOUR_2("За 2 часа", 120),
    DAY_1("За 1 день", 1440)
}

enum class DayOfWeek(val displayName: String, val dayNumber: Int) {
    MONDAY("Пн", 1),
    TUESDAY("Вт", 2),
    WEDNESDAY("Ср", 3),
    THURSDAY("Чт", 4),
    FRIDAY("Пт", 5),
    SATURDAY("Сб", 6),
    SUNDAY("Вс", 7)
}
