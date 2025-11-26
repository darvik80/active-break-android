package xyz.crearts.activebreak.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.crearts.activebreak.R
import xyz.crearts.activebreak.ui.screens.home.HomeViewModel

@Composable
fun BackgroundServiceStatusCard(
    viewModel: HomeViewModel,
    settings: xyz.crearts.activebreak.data.preferences.Settings
) {
    val context = LocalContext.current
    val isWorkManagerActive = remember { mutableStateOf(false) }
    val isBatteryOptimizationIgnored = remember { mutableStateOf(false) }

    LaunchedEffect(settings.isEnabled) {
        isWorkManagerActive.value = viewModel.checkWorkManagerStatus()
        isBatteryOptimizationIgnored.value = isIgnoringBatteryOptimizations(context)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWorkManagerActive.value && settings.isEnabled) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isWorkManagerActive.value && settings.isEnabled) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = if (isWorkManagerActive.value && settings.isEnabled) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.background_service_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(
                    onClick = {
                        isWorkManagerActive.value = viewModel.checkWorkManagerStatus()
                        isBatteryOptimizationIgnored.value = isIgnoringBatteryOptimizations(context)
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.background_service_refresh))
                }
            }

            Text(
                if (isWorkManagerActive.value && settings.isEnabled) {
                    stringResource(R.string.background_service_working)
                } else if (settings.isEnabled) {
                    stringResource(R.string.background_service_not_started)
                } else {
                    stringResource(R.string.background_service_paused)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (!isBatteryOptimizationIgnored.value) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.battery_optimization_title),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stringResource(R.string.battery_optimization_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = { requestIgnoreBatteryOptimizations(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.battery_optimization_disable))
                }
            }

            Text(
                stringResource(R.string.battery_optimization_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    return true
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Если не удалось открыть прямой запрос, открываем общие настройки
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        }
    }
}
