package xyz.crearts.activebreak.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import xyz.crearts.activebreak.ui.components.MotivationalCarousel
import xyz.crearts.activebreak.ui.components.StatisticsCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import xyz.crearts.activebreak.R
import xyz.crearts.activebreak.ui.components.PermissionNotificationCard
import xyz.crearts.activebreak.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_settings))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.activities_home)) },
                    label = { Text(stringResource(R.string.activities_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Activities.route) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MotivationalCarousel(modifier = Modifier.fillMaxWidth())
            }

            item {
                PermissionNotificationCard(
                    viewModel = viewModel,
                    settings = settings
                )
            }

            item {
                StatisticsCard(modifier = Modifier.fillMaxWidth())
            }



            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.home_active_exercises, activities.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Activities.route) }
                    ) {
                        Text(stringResource(R.string.home_manage))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.home_manage_activities),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            items(activities.take(5)) { activity ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
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
                        }
                    }
                }
            }

            if (activities.size > 5) {
                item {
                    TextButton(
                        onClick = { navController.navigate(Screen.Activities.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.home_view_all_activities))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
