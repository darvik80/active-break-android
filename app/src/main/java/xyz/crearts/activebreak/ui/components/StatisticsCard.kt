package xyz.crearts.activebreak.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.ui.components.charts.LineChart
import xyz.crearts.activebreak.ui.components.charts.PieChart
import xyz.crearts.activebreak.ui.screens.home.HomeViewModel

@Composable
fun StatisticsCard(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val todayCount by viewModel.todayCompletedCount.collectAsStateWithLifecycle()
    val weekCount by viewModel.weekCompletedCount.collectAsStateWithLifecycle()

    var showCharts by remember { mutableStateOf(false) }
    var weeklyChartData by remember { mutableStateOf(emptyList<xyz.crearts.activebreak.ui.components.charts.ChartData>()) }
    var pieChartData by remember { mutableStateOf(emptyList<xyz.crearts.activebreak.ui.components.charts.PieChartData>()) }

    val scope = rememberCoroutineScope()

    // Load chart data when switching to chart view
    LaunchedEffect(showCharts) {
        if (showCharts) {
            scope.launch {
                weeklyChartData = viewModel.getWeeklyChartData()
                pieChartData = viewModel.getActivityTypePieData()
            }
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Твоя статистика",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showCharts = !showCharts }
                    ) {
                        Icon(
                            if (showCharts) Icons.Default.Numbers else Icons.Default.BarChart,
                            contentDescription = if (showCharts) "Показать числа" else "Показать графики",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showCharts) {
                // Chart view
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    LineChart(
                        data = weeklyChartData,
                        title = "Активность по дням недели",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pieChartData.isNotEmpty()) {
                        PieChart(
                            data = pieChartData,
                            title = "Типы активности",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Numbers view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Today,
                        label = "Сегодня",
                        value = todayCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .padding(vertical = 8.dp)
                    )

                    StatItem(
                        icon = Icons.Default.CalendarMonth,
                        label = "За неделю",
                        value = weekCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}
