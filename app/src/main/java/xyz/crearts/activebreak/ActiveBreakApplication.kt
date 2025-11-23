package xyz.crearts.activebreak

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class ActiveBreakApplication : Application() {
    // DataStore как синглтон на уровне приложения
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ActiveBreakApplication
            private set
    }
}
