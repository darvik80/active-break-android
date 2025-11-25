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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.ui.components.NotificationActionDialog
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

                    // State for notification dialog
                    var showNotificationDialog by remember { mutableStateOf(false) }
                    var notificationTitle by remember { mutableStateOf("") }
                    var notificationDescription by remember { mutableStateOf<String?>(null) }
                    var isNotificationTodo by remember { mutableStateOf(false) }

                    // Check if opened from notification
                    LaunchedEffect(Unit) {
                        checkNotificationIntent { title, description, isTodo ->
                            notificationTitle = title
                            notificationDescription = description
                            isNotificationTodo = isTodo
                            showNotificationDialog = true
                        }
                    }

                    NavGraph(navController = navController)

                    // Show notification action dialog if needed
                    if (showNotificationDialog) {
                        NotificationActionDialog(
                            title = notificationTitle,
                            description = notificationDescription,
                            isTodo = isNotificationTodo,
                            onDismiss = { showNotificationDialog = false },
                            onActionCompleted = {
                                // Clear the intent to prevent showing dialog again
                                intent.removeExtra(NotificationHelper.EXTRA_FROM_NOTIFICATION)
                            }
                        )
                    }
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

    // Check if activity was opened from notification click
    private fun checkNotificationIntent(onNotificationFound: (String, String?, Boolean) -> Unit) {
        val isFromNotification = intent.getBooleanExtra(NotificationHelper.EXTRA_FROM_NOTIFICATION, false)

        if (isFromNotification) {
            val title = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_TITLE) ?: return
            val description = intent.getStringExtra(NotificationHelper.EXTRA_ACTIVITY_DESCRIPTION)
            val isTodo = intent.getBooleanExtra(NotificationHelper.EXTRA_IS_TODO, false)

            onNotificationFound(title, description, isTodo)
        }
    }
}