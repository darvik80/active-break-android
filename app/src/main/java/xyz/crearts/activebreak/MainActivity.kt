package xyz.crearts.activebreak

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.ui.components.NotificationActionDialog
import xyz.crearts.activebreak.ui.navigation.NavGraph
import xyz.crearts.activebreak.ui.navigation.Screen
import xyz.crearts.activebreak.ui.theme.ActiveBreakTheme
import xyz.crearts.activebreak.workers.NotificationHelper
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.utils.LocaleHelper

class MainActivity : ComponentActivity() {

    private var isAppActive = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Разрешение получено
            NotificationHelper.createNotificationChannel(this)
            // Show status notification after permission granted
            updateStatusNotification()
        }
    }

    // BroadcastReceiver to handle status toggle from notification
    private val statusToggleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "xyz.crearts.activebreak.ACTION_TOGGLE_APP_STATUS") {
                toggleAppStatus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language setting
        applySavedLanguage()

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

        // Register broadcast receiver for status toggle
        val filter = IntentFilter("xyz.crearts.activebreak.ACTION_TOGGLE_APP_STATUS")
        registerReceiver(statusToggleReceiver, filter, RECEIVER_NOT_EXPORTED)

        // Запускаем WorkManager если напоминания включены
        checkAndStartWorkManager()

        // Show status notification
        updateStatusNotification()

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

                    // Determine start destination based on first launch
                    var startDestination by remember { mutableStateOf(Screen.Home.route) }
                    var isInitialized by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        Log.e("MainActivity", "LaunchedEffect started - initial setup")
                        try {
                            val settingsManager = SettingsManager.instance
                            Log.e("MainActivity", "SettingsManager obtained")
                            val settings = settingsManager.getSettings().first()
                            Log.e("MainActivity", "Settings obtained: isFirstLaunch = ${settings.isFirstLaunch}")

                            startDestination = if (settings.isFirstLaunch) {
                                Log.e("MainActivity", "Setting startDestination to LanguageSelection")
                                Screen.LanguageSelection.route
                            } else {
                                Log.e("MainActivity", "Setting startDestination to Home")
                                Screen.Home.route
                            }
                            Log.e("MainActivity", "Final startDestination = $startDestination")
                            isInitialized = true
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error in LaunchedEffect: ${e.message}", e)
                            startDestination = Screen.Home.route
                            isInitialized = true
                        }
                    }

                    // Check if opened from notification
                    LaunchedEffect(Unit) {
                        checkNotificationIntent { title, description, isTodo ->
                            notificationTitle = title
                            notificationDescription = description
                            isNotificationTodo = isTodo
                            showNotificationDialog = true
                        }
                    }

                    // Only show NavGraph after initialization
                    if (isInitialized) {
                        Log.e("MainActivity", "Rendering NavGraph with startDestination: $startDestination")
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    } else {
                        Log.e("MainActivity", "Waiting for initialization...")
                        // Show loading indicator while initializing
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

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

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(statusToggleReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore
        }
    }

    private fun toggleAppStatus() {
        isAppActive = !isAppActive

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val settingsManager = xyz.crearts.activebreak.data.preferences.SettingsManager.instance
            val settings = settingsManager.getSettings().first()

            if (isAppActive && settings.isEnabled) {
                // Resume: start WorkManager
                xyz.crearts.activebreak.workers.BreakReminderWorker.scheduleWork(
                    this@MainActivity,
                    settings.intervalMinutes
                )
            } else {
                // Pause: cancel WorkManager
                xyz.crearts.activebreak.workers.BreakReminderWorker.cancelWork(this@MainActivity)
            }

            // Update status notification
            updateStatusNotification()
        }
    }

    private fun updateStatusNotification() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val settingsManager = xyz.crearts.activebreak.data.preferences.SettingsManager.instance
            val settings = settingsManager.getSettings().first()

            if (settings.isEnabled) {
                // Show status notification with current status
                NotificationHelper.showStatusNotification(this@MainActivity, isAppActive)
            } else {
                // Hide status notification if app is disabled
                NotificationHelper.hideStatusNotification(this@MainActivity)
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

    // Apply saved language setting
    private fun applySavedLanguage() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val settingsManager = SettingsManager.instance
            val settings = settingsManager.getSettings().first()
            if (settings.language != "system") {
                LocaleHelper.setLocale(this@MainActivity, settings.language)
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        // Try to get saved language synchronously from SharedPreferences
        val context = newBase?.let { ctx ->
            try {
                // Get language from SharedPreferences directly (synchronous)
                val sharedPrefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
                val savedLanguage = sharedPrefs.getString("language", "system") ?: "system"
                LocaleHelper.applyLanguage(ctx, savedLanguage)
            } catch (e: Exception) {
                // Fallback to original context if something goes wrong
                ctx
            }
        } ?: newBase

        super.attachBaseContext(context)
    }
}