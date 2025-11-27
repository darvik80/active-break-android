package xyz.crearts.activebreak.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.crearts.activebreak.data.local.dao.ActivityStatisticsDao
import xyz.crearts.activebreak.data.local.dao.BreakActivityDao
import xyz.crearts.activebreak.data.local.dao.TodoTaskDao
import xyz.crearts.activebreak.data.local.entity.ActivityStatistics
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import xyz.crearts.activebreak.data.local.entity.TodoTask

@Database(
    entities = [BreakActivity::class, TodoTask::class, ActivityStatistics::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breakActivityDao(): BreakActivityDao
    abstract fun todoTaskDao(): TodoTaskDao
    abstract fun activityStatisticsDao(): ActivityStatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var isDataPopulated = false

        private val populationMutex = Mutex()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "active_break_database"
                )
                    .fallbackToDestructiveMigration() // Временно для разработки
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Database will be populated when first accessed via ensureDefaultActivities()
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureDefaultActivities(context: Context) {
            // Early return if already populated
            if (isDataPopulated) return

            val database = getDatabase(context)
            val dao = database.breakActivityDao()

            // Use Mutex for thread-safe access with suspend functions
            populationMutex.withLock {
                // Double-check pattern
                if (isDataPopulated) return

                // Check if we have any activities at all
                val activityCount = dao.getCount()
                if (activityCount > 0) {
                    isDataPopulated = true
                    return // Data already exists
                }

                // Populate with default activities
                populateDefaultActivities(dao)
                isDataPopulated = true
            }
        }

        private suspend fun populateDefaultActivities(dao: BreakActivityDao) {

            val defaultActivities = listOf(
                // === УТРО 6-9: Энергичные активности ===
                BreakActivity(
                    title = "Утренняя зарядка 💪",
                    description = "10 приседаний для бодрости",
                    timeOfDay = "EARLY_MORNING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Планка 30 секунд 🏋️",
                    description = "Укрепление кора утром",
                    timeOfDay = "EARLY_MORNING",
                    weight = 4
                ),
                BreakActivity(
                    title = "Отжимания от стола 💪",
                    description = "10 отжиманий для тонуса",
                    timeOfDay = "EARLY_MORNING",
                    weight = 4
                ),
                BreakActivity(
                    title = "Выпить стакан воды 💧",
                    description = "Запустите метаболизм",
                    timeOfDay = "EARLY_MORNING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Прыжки на месте 🦘",
                    description = "20 прыжков для энергии",
                    timeOfDay = "EARLY_MORNING",
                    weight = 3
                ),

                // === ПОЗДНЕЕ УТРО 10-11: Разминка ===
                BreakActivity(
                    title = "Размять шею и плечи 🤸",
                    description = "Круговые движения головой",
                    timeOfDay = "LATE_MORNING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Потянуться всем телом 🧘",
                    description = "Растяжка снимет напряжение",
                    timeOfDay = "LATE_MORNING",
                    weight = 4
                ),
                BreakActivity(
                    title = "Растяжка запястий ✋",
                    description = "Для тех кто работает за компьютером",
                    timeOfDay = "LATE_MORNING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Выпить воды 💧",
                    description = "Поддержание водного баланса",
                    timeOfDay = "LATE_MORNING",
                    weight = 4
                ),

                // === ОБЕД 12-14: Легкая активность ===
                BreakActivity(
                    title = "Прогулка 5 минут 🚶",
                    description = "Выйдите на улицу",
                    timeOfDay = "MIDDAY",
                    weight = 5
                ),
                BreakActivity(
                    title = "Лёгкая растяжка 🧘",
                    description = "После обеда особенно важна",
                    timeOfDay = "MIDDAY",
                    weight = 4
                ),
                BreakActivity(
                    title = "Подняться по лестнице 🏃",
                    description = "2-3 пролета",
                    timeOfDay = "MIDDAY",
                    weight = 3
                ),
                BreakActivity(
                    title = "Глубокое дыхание 🌬️",
                    description = "5 глубоких вдохов",
                    timeOfDay = "MIDDAY",
                    weight = 4
                ),

                // === ДЕНЬ 15-17: Фокус на глаза и концентрацию ===
                BreakActivity(
                    title = "Правило 20-20-20 для глаз 👁️",
                    description = "Смотрите на 20 метров 20 секунд",
                    timeOfDay = "AFTERNOON",
                    weight = 6
                ),
                BreakActivity(
                    title = "Гимнастика для глаз 👁️",
                    description = "Круговые движения глазами",
                    timeOfDay = "AFTERNOON",
                    weight = 6
                ),
                BreakActivity(
                    title = "Посмотреть в окно 👀",
                    description = "2 минуты отдыха для глаз",
                    timeOfDay = "AFTERNOON",
                    weight = 5
                ),
                BreakActivity(
                    title = "Прогулка до окна 🚶",
                    description = "Короткая прогулка + отдых для глаз",
                    timeOfDay = "AFTERNOON",
                    weight = 5
                ),
                BreakActivity(
                    title = "Перекусить фруктом 🍎",
                    description = "Здоровый перекус даст энергию",
                    timeOfDay = "AFTERNOON",
                    weight = 3
                ),
                BreakActivity(
                    title = "Выпить чай 🍵",
                    description = "Теплый напиток расслабит",
                    timeOfDay = "AFTERNOON",
                    weight = 4
                ),

                // === ВЕЧЕР 18-20: Возвращение активности ===
                BreakActivity(
                    title = "Приседания 15 раз 💪",
                    description = "Разомните мышцы",
                    timeOfDay = "EVENING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Планка 45 секунд 🏋️",
                    description = "Укрепление тела",
                    timeOfDay = "EVENING",
                    weight = 4
                ),
                BreakActivity(
                    title = "Растяжка спины 🤸",
                    description = "Йога поза кошки-коровы",
                    timeOfDay = "EVENING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Прогулка 10 минут 🚶",
                    description = "Вечерняя активность",
                    timeOfDay = "EVENING",
                    weight = 4
                ),
                BreakActivity(
                    title = "Наклоны в стороны 🤸",
                    description = "Растяжка боковых мышц",
                    timeOfDay = "EVENING",
                    weight = 4
                ),

                // === ПОЗДНИЙ ВЕЧЕР 21-23: Релаксация ===
                BreakActivity(
                    title = "Дыхание 4-7-8 😮‍💨",
                    description = "Расслабляющая техника",
                    timeOfDay = "LATE_EVENING",
                    weight = 6
                ),
                BreakActivity(
                    title = "Медитация 3 минуты 🧘‍♂️",
                    description = "Закройте глаза и расслабьтесь",
                    timeOfDay = "LATE_EVENING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Лёгкая растяжка 🧘",
                    description = "Подготовка ко сну",
                    timeOfDay = "LATE_EVENING",
                    weight = 5
                ),
                BreakActivity(
                    title = "Послушать музыку 🎵",
                    description = "Спокойная мелодия",
                    timeOfDay = "LATE_EVENING",
                    weight = 4
                ),

                // === УНИВЕРСАЛЬНЫЕ (ANY) ===
                BreakActivity(
                    title = "Встать и выпить воды 💧",
                    description = "Всегда важно пить воду",
                    timeOfDay = "ANY",
                    weight = 4
                ),
                BreakActivity(
                    title = "Размять кисти рук ✋",
                    description = "Профилактика туннельного синдрома",
                    timeOfDay = "ANY",
                    weight = 4
                ),
                BreakActivity(
                    title = "Потянуться 🧘",
                    description = "Быстрая растяжка",
                    timeOfDay = "ANY",
                    weight = 4
                ),
                BreakActivity(
                    title = "Глубокое дыхание 🌬️",
                    description = "5 вдохов-выдохов",
                    timeOfDay = "ANY",
                    weight = 3
                ),
                BreakActivity(
                    title = "Встать и пройтись 🚶",
                    description = "Минутная прогулка",
                    timeOfDay = "ANY",
                    weight = 3
                )
            )

            // Insert all default activities
            defaultActivities.forEach { dao.insert(it) }
        }
    }
}
