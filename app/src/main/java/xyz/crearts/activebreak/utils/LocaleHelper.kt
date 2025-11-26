package xyz.crearts.activebreak.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {

    /**
     * Apply language setting to context
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = getLocaleFromCode(languageCode)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Set locale globally and update app resources
     */
    fun setLocale(context: Context, languageCode: String) {
        val locale = getLocaleFromCode(languageCode)

        // Set default locale
        Locale.setDefault(locale)

        // Update app resources configuration
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)

        // Update resources with new configuration
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Set locale globally (fallback method without context)
     */
    fun setLocale(languageCode: String) {
        val locale = getLocaleFromCode(languageCode)
        Locale.setDefault(locale)
    }

    /**
     * Get locale from language code
     */
    private fun getLocaleFromCode(languageCode: String): Locale {
        return when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            "system" -> Locale.getDefault()
            else -> Locale.getDefault()
        }
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
     * Get available languages for first launch (English/Russian only)
     */
    fun getFirstLaunchLanguages(): List<Pair<String, String>> {
        return listOf(
            "ru" to "Русский",
            "en" to "English"
        )
    }

    /**
     * Get available languages for settings (System/English/Russian)
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "system" to "System",
            "ru" to "Русский",
            "en" to "English"
        )
    }
}
