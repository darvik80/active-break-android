package xyz.crearts.activebreak.ui.screens.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import xyz.crearts.activebreak.data.local.entity.BreakActivity
import xyz.crearts.activebreak.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    navController: NavController,
    viewModel: ActivitiesViewModel = viewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Активности") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Активности") },
                    label = { Text("Активности") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Todo.route) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "TODO") },
                    label = { Text("TODO") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val groupedActivities = activities.groupBy { 
                when (it.timeOfDay) {
                    "EARLY_MORNING" -> "🌅 Утро (6-9)"
                    "LATE_MORNING" -> "☀️ Позднее утро (10-11)"
                    "MIDDAY" -> "🌞 Обед (12-14)"
                    "AFTERNOON" -> "🌤️ День (15-17)"
                    "EVENING" -> "🌆 Вечер (18-20)"
                    "LATE_EVENING" -> "🌙 Поздний вечер (21-23)"
                    "ANY" -> "⏰ В любое время"
                    else -> "Другое"
                }
            }

            // Сортируем группы в логическом порядке
            val sortedGroups = groupedActivities.toSortedMap(compareBy { key ->
                when (key) {
                    "🌅 Утро (6-9)" -> 1
                    "☀️ Позднее утро (10-11)" -> 2
                    "🌞 Обед (12-14)" -> 3
                    "🌤️ День (15-17)" -> 4
                    "🌆 Вечер (18-20)" -> 5
                    "🌙 Поздний вечер (21-23)" -> 6
                    "⏰ В любое время" -> 7
                    else -> 8
                }
            })

            sortedGroups.forEach { (timeLabel, activitiesInGroup) ->
                item {
                    Text(
                        timeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(activitiesInGroup) { activity ->
                    ActivityCard(
                        activity = activity,
                        onToggleActive = { viewModel.toggleActivityActive(activity) },
                        onDelete = { viewModel.deleteActivity(activity) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, timeOfDay, weight ->
                viewModel.addActivity(title, description, timeOfDay, weight)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ActivityCard(
    activity: BreakActivity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val timeOptions = mapOf(
        "EARLY_MORNING" to "Утро",
        "LATE_MORNING" to "Позднее утро",
        "MIDDAY" to "Обед",
        "AFTERNOON" to "День",
        "EVENING" to "Вечер",
        "LATE_EVENING" to "Поздний вечер",
        "ANY" to "Любое"
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = activity.isActive,
                onCheckedChange = { onToggleActive() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!activity.description.isNullOrBlank()) {
                    Text(
                        activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(timeOptions[activity.timeOfDay] ?: activity.timeOfDay) },
                        modifier = Modifier.height(24.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Вес: ${activity.weight}") },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("ANY") }
    var weight by remember { mutableStateOf(3f) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val timeOptions = mapOf(
        "EARLY_MORNING" to "🌅 Утро (6-9)",
        "LATE_MORNING" to "☀️ Позднее утро (10-11)",
        "MIDDAY" to "🌞 Обед (12-14)",
        "AFTERNOON" to "🌤️ День (15-17)",
        "EVENING" to "🌆 Вечер (18-20)",
        "LATE_EVENING" to "🌙 Поздний вечер (21-23)",
        "ANY" to "⏰ В любое время"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая активность") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (опционально)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Время: ${timeOptions[timeOfDay] ?: "Выбрать"}")
                }

                Text("Вес (приоритет): ${weight.toInt()}")
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 1f..10f,
                    steps = 8
                )

                if (showTimePickerDialog) {
                    AlertDialog(
                        onDismissRequest = { showTimePickerDialog = false },
                        title = { Text("Выберите время") },
                        text = {
                            Column {
                                timeOptions.forEach { (key, label) ->
                                    TextButton(
                                        onClick = {
                                            timeOfDay = key
                                            showTimePickerDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(label, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showTimePickerDialog = false }) {
                                Text("Закрыть")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description.takeIf { it.isNotBlank() }, timeOfDay, weight.toInt())
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
