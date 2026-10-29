package com.carlauncher.auto.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import com.carlauncher.auto.model.MediaTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central controller for active car media sessions (Spotify, local radio, player apps).
 */
object MediaControllerManager {

    private const val TAG = "MediaControllerManager"
    const val SPOTIFY_PACKAGE = "com.spotify.music"

    private val _currentTrack = MutableStateFlow<MediaTrack?>(null)
    val currentTrack: StateFlow<MediaTrack?> = _currentTrack.asStateFlow()

    private var activeController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateTrackFromController()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateTrackFromController()
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            activeController = null
            _currentTrack.value = null
        }
    }

    fun setActiveController(controller: MediaController?) {
        if (activeController?.sessionToken == controller?.sessionToken && activeController != null) {
            updateTrackFromController()
            return
        }

        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering previous controller callback", e)
        }

        activeController = controller

        if (controller != null) {
            try {
                controller.registerCallback(controllerCallback, mainHandler)
                updateTrackFromController()
            } catch (e: Exception) {
                Log.e(TAG, "Error registering media callback", e)
            }
        } else {
            _currentTrack.value = null
        }
    }

    fun updateTrackFromController() {
        val controller = activeController ?: return
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)

        val albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L
        val packageName = controller.packageName

        val track = MediaTrack(
            title = title,
            artist = artist,
            album = album,
            albumArt = albumArt,
            isPlaying = isPlaying,
            duration = duration,
            position = position,
            packageName = packageName,
            appName = if (packageName == SPOTIFY_PACKAGE) "Spotify" else null
        )

        _currentTrack.value = track
    }

    fun playPause() {
        val controller = activeController
        val state = controller?.playbackState?.state

        if (controller != null && state != null) {
            if (state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs)
    }

    /**
     * Deep-links or launches Spotify app natively on the head unit.
     */
    fun openSpotify(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(SPOTIFY_PACKAGE)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            // If not found, launch via generic music intent
            val musicIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(musicIntent)
            } catch (e: Exception) {
                Log.e(TAG, "No music app found to launch", e)
            }
        }
    }
}
