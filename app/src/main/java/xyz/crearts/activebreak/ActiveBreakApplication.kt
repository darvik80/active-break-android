package xyz.crearts.activebreak

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.utils.LocaleHelper

class ActiveBreakApplication : Application() {
    // DataStore as application-level singleton
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    // SettingsManager as application-level singleton
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize SettingsManager safely after Application is created
        settingsManager = SettingsManager(dataStore)

        // Apply saved language setting on app start
        applySavedLanguage()
    }

    override fun attachBaseContext(base: Context?) {
        // Apply language setting before creating application context
        val context = base?.let { ctx ->
            try {
                // Get language from SharedPreferences directly (synchronous)
                val sharedPrefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
                val savedLanguage = sharedPrefs.getString("language", "system") ?: "system"
                LocaleHelper.applyLanguage(ctx, savedLanguage)
            } catch (e: Exception) {
                // Fallback to original context if something goes wrong
                ctx
            }
        } ?: base

        super.attachBaseContext(context)
    }

    private fun applySavedLanguage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsManager.getSettings().first()
                if (settings.language != "system") {
                    LocaleHelper.setLocale(this@ActiveBreakApplication, settings.language)
                }
            } catch (e: Exception) {
                // Ignore errors during language application
            }
        }
    }

    companion object {
        lateinit var instance: ActiveBreakApplication
            private set
    }
}
