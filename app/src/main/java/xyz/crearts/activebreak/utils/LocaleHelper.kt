package xyz.crearts.activebreak.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    
    /**
     * Apply language setting to context
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            "system" -> Locale.getDefault()
            else -> Locale.getDefault()
        }
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * Get display name for language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "ru" -> "Русский"
            "en" -> "English"
            "system" -> "System"
            else -> "System"
        }
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "system" to "System",
            "ru" to "Русский",
            "en" to "English"
        )
    }
}
