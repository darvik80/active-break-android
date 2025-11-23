package xyz.crearts.activebreak.ui.screens.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import xyz.crearts.activebreak.data.local.entity.TodoTask
import xyz.crearts.activebreak.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(it) }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DaysOfWeekPickerDialog(
    selectedDays: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    var currentSelection by remember { mutableStateOf(selectedDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите дни недели") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysOfWeek.forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            getDayFullName(day),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Checkbox(
                            checked = currentSelection.contains(day),
                            onCheckedChange = { checked ->
                                currentSelection = if (checked) {
                                    currentSelection + day
                                } else {
                                    currentSelection - day
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentSelection) },
                enabled = currentSelection.isNotEmpty()
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun getDayFullName(shortName: String): String {
    return when (shortName) {
        "Пн" -> "Понедельник"
        "Вт" -> "Вторник"
        "Ср" -> "Среда"
        "Чт" -> "Четверг"
        "Пт" -> "Пятница"
        "Сб" -> "Суббота"
        "Вс" -> "Воскресенье"
        else -> shortName
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int = 12,
    initialMinute: Int = 0
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { hour = (hour + 1) % 24 }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить час")
                    }
                    Text(
                        String.format(Locale.getDefault(), "%02d", hour),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить час")
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))

                // Minute picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 5) % 60 }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить минуты")
                    }
                    Text(
                        String.format(Locale.getDefault(), "%02d", minute),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { minute = if (minute < 5) 55 else minute - 5 }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить минуты")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    navController: NavController,
    viewModel: TodoViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoTask?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TODO-список") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
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
                    selected = false,
                    onClick = { navController.navigate(Screen.Activities.route) },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Активности") },
                    label = { Text("Активности") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "TODO") },
                    label = { Text("TODO") }
                )
            }
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Нет задач",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Добавьте первую задачу",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    CompactTodoTaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = { editingTask = task },
                        onPause = { viewModel.toggleTaskPause(task) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, dueDate, hour, minute, recurrence, days, reminderEnabled, reminderMinutes, category ->
                viewModel.addTask(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    hour = hour,
                    minute = minute,
                    recurrenceType = recurrence,
                    recurrenceDays = days,
                    reminderEnabled = reminderEnabled,
                    reminderMinutesBefore = reminderMinutes,
                    category = category
                )
                showAddDialog = false
            },
            existingTask = null
        )
    }

    editingTask?.let { task ->
        AddTaskDialog(
            onDismiss = { editingTask = null },
            onAdd = { title, description, dueDate, hour, minute, recurrence, days, reminderEnabled, reminderMinutes, category ->
                viewModel.updateTask(task.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    category = category,
                    recurrenceType = recurrence,
                    recurrenceDays = days,
                    reminderEnabled = reminderEnabled,
                    reminderMinutesBefore = reminderMinutes
                ))
                editingTask = null
            },
            existingTask = task
        )
    }
}

@Composable
fun CompactTodoTaskCard(
    task: TodoTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPause: () -> Unit
) {
    val category = xyz.crearts.activebreak.domain.model.TodoCategory.fromString(task.category)
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isPaused) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Чекбокс (компактный)
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Иконка категории
                Icon(
                    category.icon,
                    contentDescription = category.displayName,
                    tint = if (task.isCompleted || task.isPaused) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Основная информация (заголовок и время)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (task.isPaused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Краткая информация о времени/дате, если есть
                    task.dueDate?.let { date ->
                        val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                         Text(
                            dateFormat.format(Date(date)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Индикатор развертывания или действий
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть"
                    )
                }
            }

            // Развернутая информация
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp, start = 36.dp)) {
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Чипсы с деталями
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Периодичность
                        if (task.recurrenceType != "NONE") {
                            val recurrenceText = when (task.recurrenceType) {
                                "DAILY" -> "Ежедневно"
                                "WEEKLY" -> "Еженедельно"
                                "BIWEEKLY" -> "Раз в 2 нед"
                                "MONTHLY" -> "Ежемесячно"
                                "CUSTOM_DAYS" -> task.recurrenceDays ?: "По дням"
                                else -> ""
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(recurrenceText, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Default.Repeat, null, Modifier.size(14.dp)) },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        // Напоминание
                        if (task.reminderEnabled) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${task.reminderMinutesBefore} мин", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Default.Notifications, null, Modifier.size(14.dp)) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        
                        if (task.isPaused) {
                             AssistChip(
                                onClick = {},
                                label = { Text("На паузе", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Default.Pause, null, Modifier.size(14.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onPause) {
                            Icon(if (task.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (task.isPaused) "Возобновить" else "Пауза")
                        }
                        TextButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Изменить")
                        }
                        TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?, Long?, Int, Int, String, String?, Boolean, Int, String) -> Unit,
    existingTask: TodoTask? = null
) {
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var showRecurrencePicker by remember { mutableStateOf(false) }
    var selectedRecurrence by remember { mutableStateOf(
        when (existingTask?.recurrenceType) {
            "DAILY" -> "Ежедневно"
            "WEEKLY" -> "Еженедельно"
            "BIWEEKLY" -> "Раз в 2 недели"
            "MONTHLY" -> "Ежемесячно"
            "CUSTOM_DAYS" -> "По дням недели"
            else -> "Разовая"
        }
    ) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(existingTask?.reminderEnabled ?: false) }
    var reminderMinutes by remember { mutableIntStateOf(existingTask?.reminderMinutesBefore ?: 15) }
    var selectedReminder by remember { mutableStateOf(
        when (existingTask?.reminderMinutesBefore) {
            5 -> "За 5 минут"
            15 -> "За 15 минут"
            30 -> "За 30 минут"
            60 -> "За 1 час"
            120 -> "За 2 часа"
            1440 -> "За 1 день"
            else -> "За 15 минут"
        }
    ) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(existingTask?.dueDate) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(
        existingTask?.dueDate?.let { 
            java.util.Calendar.getInstance().apply { timeInMillis = it }.get(java.util.Calendar.HOUR_OF_DAY) 
        } ?: 12
    ) }
    var selectedMinute by remember { mutableIntStateOf(
        existingTask?.dueDate?.let { 
            java.util.Calendar.getInstance().apply { timeInMillis = it }.get(java.util.Calendar.MINUTE) 
        } ?: 0
    ) }
    var showDaysOfWeekPicker by remember { mutableStateOf(false) }
    var selectedDaysOfWeek by remember { mutableStateOf(
        existingTask?.recurrenceDays?.split(",")?.toSet() ?: setOf()
    ) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(
        xyz.crearts.activebreak.domain.model.TodoCategory.fromString(
            existingTask?.category ?: "OTHER"
        )
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTask != null) "Редактировать задачу" else "Новая задача") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Категория
                OutlinedButton(
                    onClick = { showCategoryPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(selectedCategory.icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${selectedCategory.emoji} ${selectedCategory.displayName}")
                }

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Дата и время
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedDate != null) {
                            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                            "Дата: ${dateFormat.format(java.util.Date(selectedDate!!))}"
                        } else {
                            "Выбрать дату"
                        }
                    )
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Время: ${String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)}"
                    )
                }

                // Периодичность
                OutlinedButton(
                    onClick = { showRecurrencePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Повтор: $selectedRecurrence")
                        if (selectedRecurrence == "По дням недели" && selectedDaysOfWeek.isNotEmpty()) {
                            Text(
                                selectedDaysOfWeek.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Напоминание
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Напоминание", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (reminderEnabled) {
                    OutlinedButton(
                        onClick = { showReminderPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedReminder)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val recurrenceType = when (selectedRecurrence) {
                            "Ежедневно" -> "DAILY"
                            "Еженедельно" -> "WEEKLY"
                            "Раз в 2 недели" -> "BIWEEKLY"
                            "Ежемесячно" -> "MONTHLY"
                            "По дням недели" -> "CUSTOM_DAYS"
                            else -> "NONE"
                        }

                        onAdd(
                            title,
                            description.takeIf { it.isNotBlank() },
                            selectedDate,
                            selectedHour,
                            selectedMinute,
                            recurrenceType,
                            if (selectedDaysOfWeek.isNotEmpty()) selectedDaysOfWeek.joinToString(",") else null,
                            reminderEnabled,
                            reminderMinutes,
                            selectedCategory.name
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (existingTask != null) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    // Диалог выбора периодичности
    if (showRecurrencePicker) {
        AlertDialog(
            onDismissRequest = { showRecurrencePicker = false },
            title = { Text("Периодичность") },
            text = {
                Column {
                    listOf("Разовая", "Ежедневно", "Еженедельно", "Раз в 2 недели", "Ежемесячно", "По дням недели").forEach { option ->
                        TextButton(
                            onClick = {
                                selectedRecurrence = option
                                showRecurrencePicker = false
                                if (option == "По дням недели") {
                                    showDaysOfWeekPicker = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRecurrencePicker = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Диалог выбора времени напоминания
    if (showReminderPicker) {
        AlertDialog(
            onDismissRequest = { showReminderPicker = false },
            title = { Text("Время напоминания") },
            text = {
                Column {
                    listOf(
                        "За 5 минут" to 5,
                        "За 15 минут" to 15,
                        "За 30 минут" to 30,
                        "За 1 час" to 60,
                        "За 2 часа" to 120,
                        "За 1 день" to 1440
                    ).forEach { (label, minutes) ->
                        TextButton(
                            onClick = {
                                selectedReminder = label
                                reminderMinutes = minutes
                                showReminderPicker = false
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
                TextButton(onClick = { showReminderPicker = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { dateMillis ->
                selectedDate = dateMillis
                showDatePicker = false
            }
        )
    }

    // Category Picker
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Выберите категорию") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    xyz.crearts.activebreak.domain.model.TodoCategory.values().forEach { category ->
                        TextButton(
                            onClick = {
                                selectedCategory = category
                                showCategoryPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${category.emoji} ${category.displayName}",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Time Picker
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            },
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
    }

    // Days of Week Picker
    if (showDaysOfWeekPicker) {
        DaysOfWeekPickerDialog(
            selectedDays = selectedDaysOfWeek,
            onDismiss = { showDaysOfWeekPicker = false },
            onConfirm = { days ->
                selectedDaysOfWeek = days
                showDaysOfWeekPicker = false
            }
        )
    }
}
