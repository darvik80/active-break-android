package xyz.crearts.activebreak

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import xyz.crearts.activebreak.data.preferences.SettingsManager

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
    }

    companion object {
        lateinit var instance: ActiveBreakApplication
            private set
    }
}
