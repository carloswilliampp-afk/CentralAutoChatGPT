package com.carlauncher.auto.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Manages vehicle GPS location and calculates real-time speedometer readings in km/h.
 */
object LocationSpeedManager : LocationListener {

    private const val TAG = "LocationSpeedManager"

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _currentSpeedKmH = MutableStateFlow(0)
    val currentSpeedKmH: StateFlow<Int> = _currentSpeedKmH.asStateFlow()

    private var locationManager: LocationManager? = null
    private var isListening = false

    @SuppressLint("MissingPermission")
    fun startListening(context: Context) {
        if (isListening) return

        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

            val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            val hasNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

            if (hasGps) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L, // 0.5s updates for smooth car driving
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            if (hasNetwork) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            // Get last known location for quick initial positioning
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLast = lastGps ?: lastNetwork
            if (bestLast != null) {
                onLocationChanged(bestLast)
            }

            isListening = true
            Log.d(TAG, "GPS location updates started.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            locationManager?.removeUpdates(this)
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates", e)
        }
    }

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location

        if (location.hasSpeed()) {
            val speedMps = location.speed
            val speedKmh = (speedMps * 3.6f).roundToInt()
            // Ignore minor jitter when parked/stopped
            _currentSpeedKmH.value = if (speedKmh < 3) 0 else speedKmh
        } else {
            // Speed not reported directly by hardware; fallback to 0 if static
            _currentSpeedKmH.value = 0
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
