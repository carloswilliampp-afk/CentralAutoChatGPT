package com.carlauncher.auto.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.AudioManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import com.carlauncher.auto.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppLauncherHelper {

    private const val TAG = "AppLauncherHelper"
    const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    const val WAZE_PACKAGE = "com.waze"

    suspend fun getInstalledApps(context: Context): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val currentPackage = context.packageName
        val appList = mutableListOf<AppItem>()

        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            // Exclude this launcher itself from the app drawer
            if (packageName == currentPackage) continue

            val label = info.loadLabel(packageManager).toString()
            val activityName = info.activityInfo.name
            val icon = info.loadIcon(packageManager)

            appList.add(AppItem(label, packageName, activityName, icon))
        }

        // Sort alphabetically
        appList.sortedBy { it.label.lowercase() }
    }

    fun launchApp(context: Context, packageName: String, activityName: String) {
        try {
            val intent = Intent().apply {
                component = ComponentName(packageName, activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $packageName/$activityName", e)
            // Fallback: try launch intent for package
            val fallback = context.packageManager.getLaunchIntentForPackage(packageName)
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            } else {
                Toast.makeText(context, "Não foi possível abrir o app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchGoogleMaps(context: Context, query: String? = null) {
        try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("google.navigation:q=${Uri.encode(query)}")
            } else {
                Uri.parse("geo:0,0?q=")
            }

            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(GOOGLE_MAPS_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Try opening without package constraint or launch by package
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(GOOGLE_MAPS_PACKAGE)
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                } else {
                    Toast.makeText(context, "Google Maps não encontrado na central", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Google Maps", e)
        }
    }

    fun launchWaze(context: Context, query: String? = null) {
        try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("waze://?q=${Uri.encode(query)}&navigate=yes")
            } else {
                Uri.parse("waze://")
            }

            val wazeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(WAZE_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (wazeIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(wazeIntent)
            } else {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(WAZE_PACKAGE)
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                } else {
                    Toast.makeText(context, "Waze não encontrado na central", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Waze", e)
        }
    }

    fun launchVoiceAssistant(context: Context) {
        try {
            val voiceIntent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ouvindo...")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(voiceIntent)
        } catch (e: Exception) {
            // Fallback: standard voice recognition
            try {
                val searchIntent = Intent(RecognizerIntent.ACTION_WEB_SEARCH).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(searchIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Assistente de voz indisponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openSystemSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open Settings", e)
        }
    }

    fun openVolumePanel(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
            Log.e(TAG, "Could not show volume panel", e)
        }
    }
}
