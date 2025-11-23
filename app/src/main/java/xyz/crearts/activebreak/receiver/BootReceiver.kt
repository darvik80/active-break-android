package xyz.crearts.activebreak.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.workers.BreakReminderWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, starting WorkManager")

            // Запускаем WorkManager если напоминания включены
            runBlocking {
                val settingsManager = SettingsManager.instance
                val settings = settingsManager.getSettings().first()

                if (settings.isEnabled) {
                    BreakReminderWorker.scheduleWork(
                        context,
                        settings.intervalMinutes
                    )
                    Log.d("BootReceiver", "WorkManager started with interval: ${settings.intervalMinutes}")
                }
            }
        }
    }
}
