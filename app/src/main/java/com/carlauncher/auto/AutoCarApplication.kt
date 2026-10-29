package com.carlauncher.auto

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class AutoCarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure OSMDroid for Automotive map display and fast local caching
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }
    }
}
