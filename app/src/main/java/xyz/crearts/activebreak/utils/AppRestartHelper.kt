package xyz.crearts.activebreak.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import xyz.crearts.activebreak.MainActivity

/**
 * Helper class for app restart functionality
 */
object AppRestartHelper {
    
    /**
     * Restart the application
     * @param context Current context
     */
    fun restartApp(context: Context) {
        try {


            // Create restart intent
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Start the new activity
            context.startActivity(intent)

            // Close current activity
            if (context is Activity) {
                context.finishAffinity()
            }

            // Give some time for the new activity to start before killing process
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    android.os.Process.killProcess(android.os.Process.myPid())
                } catch (e: Exception) {
                    // If kill process fails, try System.exit as fallback
                    System.exit(0)
                }
            }, 100) // 100ms delay

        } catch (e: Exception) {
            Log.e("AppRestartHelper", "Error restarting app: ${e.message}", e)

            // Ultimate fallback - just try to recreate the activity
            if (context is Activity) {
                try {
                    context.recreate()
                } catch (recreateException: Exception) {

                }
            }
        }
    }
}
