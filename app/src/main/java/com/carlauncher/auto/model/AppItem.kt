package com.carlauncher.auto.model

import android.graphics.drawable.Drawable

/**
 * Model representing an installed native application on the car head unit.
 */
data class AppItem(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)
