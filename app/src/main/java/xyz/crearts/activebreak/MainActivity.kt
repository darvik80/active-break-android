package xyz.crearts.activebreak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.ui.navigation.NavGraph
import xyz.crearts.activebreak.ui.theme.ActiveBreakTheme
import xyz.crearts.activebreak.workers.NotificationHelper

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Разрешение получено
            NotificationHelper.createNotificationChannel(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрашиваем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationHelper.createNotificationChannel(this)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationHelper.createNotificationChannel(this)
        }

        enableEdgeToEdge()

        // Запускаем WorkManager если напоминания включены
        checkAndStartWorkManager()

        setContent {
            ActiveBreakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    private fun checkAndStartWorkManager() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val settingsManager = xyz.crearts.activebreak.data.preferences.SettingsManager.instance
            val settings = settingsManager.getSettings().first()

            if (settings.isEnabled) {
                xyz.crearts.activebreak.workers.BreakReminderWorker.scheduleWork(
                    this@MainActivity,
                    settings.intervalMinutes
                )
            }
        }
    }
}