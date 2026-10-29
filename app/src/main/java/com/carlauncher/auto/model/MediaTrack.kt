package com.carlauncher.auto.model

import android.graphics.Bitmap

/**
 * Model representing the currently active playback track (Spotify, Radio, YouTube Music, etc.).
 */
data class MediaTrack(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val position: Long = 0L,
    val packageName: String? = null,
    val appName: String? = null
)
