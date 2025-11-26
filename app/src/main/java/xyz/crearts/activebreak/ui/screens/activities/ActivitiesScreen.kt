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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import xyz.crearts.activebreak.R
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
                title = { Text(stringResource(R.string.activities_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.activities_add))
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.activities_home)) },
                    label = { Text(stringResource(R.string.activities_home)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = stringResource(R.string.activities_title)) },
                    label = { Text(stringResource(R.string.activities_title)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Todo.route) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.activities_todo)) },
                    label = { Text(stringResource(R.string.activities_todo)) }
                )
            }
        }
    ) { paddingValues ->
        // Get localized time labels
        val timeEarlyMorning = stringResource(R.string.time_early_morning)
        val timeLateMorning = stringResource(R.string.time_late_morning)
        val timeMidday = stringResource(R.string.time_midday)
        val timeAfternoon = stringResource(R.string.time_afternoon)
        val timeEvening = stringResource(R.string.time_evening)
        val timeLateEvening = stringResource(R.string.time_late_evening)
        val timeAny = stringResource(R.string.time_any)
        val timeOther = stringResource(R.string.time_other)

        val groupedActivities = activities.groupBy {
            when (it.timeOfDay) {
                "EARLY_MORNING" -> timeEarlyMorning
                "LATE_MORNING" -> timeLateMorning
                "MIDDAY" -> timeMidday
                "AFTERNOON" -> timeAfternoon
                "EVENING" -> timeEvening
                "LATE_EVENING" -> timeLateEvening
                "ANY" -> timeAny
                else -> timeOther
            }
        }

        // Sort groups in logical order
        val sortedGroups = groupedActivities.toSortedMap(compareBy { key ->
            when (key) {
                timeEarlyMorning -> 1
                timeLateMorning -> 2
                timeMidday -> 3
                timeAfternoon -> 4
                timeEvening -> 5
                timeLateEvening -> 6
                timeAny -> 7
                else -> 8
            }
        })

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

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
        "EARLY_MORNING" to stringResource(R.string.time_morning),
        "LATE_MORNING" to stringResource(R.string.time_late_morning_short),
        "MIDDAY" to stringResource(R.string.time_lunch),
        "AFTERNOON" to stringResource(R.string.time_day),
        "EVENING" to stringResource(R.string.time_evening_short),
        "LATE_EVENING" to stringResource(R.string.time_late_evening_short),
        "ANY" to stringResource(R.string.time_any_short)
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
                        label = { Text(stringResource(R.string.activity_weight_label, activity.weight)) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.activities_delete))
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
        "EARLY_MORNING" to stringResource(R.string.time_early_morning),
        "LATE_MORNING" to stringResource(R.string.time_late_morning),
        "MIDDAY" to stringResource(R.string.time_midday),
        "AFTERNOON" to stringResource(R.string.time_afternoon),
        "EVENING" to stringResource(R.string.time_evening),
        "LATE_EVENING" to stringResource(R.string.time_late_evening),
        "ANY" to stringResource(R.string.time_any)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.activity_new)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.activity_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.activity_description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.activity_time, timeOptions[timeOfDay] ?: stringResource(R.string.activity_time_select)))
                }

                Text(stringResource(R.string.activity_weight, weight.toInt()))
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 1f..10f,
                    steps = 8
                )

                if (showTimePickerDialog) {
                    AlertDialog(
                        onDismissRequest = { showTimePickerDialog = false },
                        title = { Text(stringResource(R.string.activity_select_time)) },
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
                                Text(stringResource(R.string.activity_close))
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
                Text(stringResource(R.string.activity_add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
