package com.carlauncher.auto.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * NotificationListenerService that gains access to active MediaSessions across the Android head unit.
 * Automatically hooks into Spotify or any playing media player.
 */
class CarMediaListenerService : NotificationListenerService() {

    private val TAG = "CarMediaListener"
    private var mediaSessionManager: MediaSessionManager? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handleActiveSessions(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected. Initializing MediaSession listener...")

        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, CarMediaListenerService::class.java)

            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)

            val initialSessions = mediaSessionManager?.getActiveSessions(componentName)
            handleActiveSessions(initialSessions)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Notification access permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaSessionManager", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing session listener", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Refresh active sessions when a new media notification is posted
        val componentName = ComponentName(this, CarMediaListenerService::class.java)
        try {
            val sessions = mediaSessionManager?.getActiveSessions(componentName)
            handleActiveSessions(sessions)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh sessions on notification post", e)
        }
    }

    private fun handleActiveSessions(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            Log.d(TAG, "No active media sessions found.")
            return
        }

        // Priority 1: Spotify
        val spotifyController = controllers.firstOrNull {
            it.packageName == MediaControllerManager.SPOTIFY_PACKAGE
        }

        // Priority 2: Any controller currently playing
        val playingController = controllers.firstOrNull {
            it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        }

        // Priority 3: First available controller
        val targetController = spotifyController ?: playingController ?: controllers.firstOrNull()

        MediaControllerManager.setActiveController(targetController)
    }
}
