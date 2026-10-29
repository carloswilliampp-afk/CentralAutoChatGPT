package com.carlauncher.auto

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.carlauncher.auto.databinding.ActivityMainBinding
import com.carlauncher.auto.service.CarMediaListenerService
import com.carlauncher.auto.service.MediaControllerManager
import com.carlauncher.auto.ui.apps.AppsFragment
import com.carlauncher.auto.ui.dashboard.DashboardFragment
import com.carlauncher.auto.utils.AppLauncherHelper
import com.carlauncher.auto.utils.LocationSpeedManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isAppsViewVisible = false
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, 1000)
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            LocationSpeedManager.startListening(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep head unit screen always on while driving
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Setup Fullscreen Immersive Mode for 1920x1080 automotive panel
        setupImmersiveMode()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initial fragment is the Android Auto Dashboard
        if (savedInstanceState == null) {
            showDashboard()
        }

        setupDockBar()
        checkPermissions()
        setupBackPressed()
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
        startClock()
        LocationSpeedManager.startListening(this)
        MediaControllerManager.updateTrackFromController()
    }

    override fun onPause() {
        super.onPause()
        stopClock()
        LocationSpeedManager.stopListening()
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupDockBar() {
        // 9-dot launcher icon: Toggles between Dashboard and App Grid
        binding.dockBar.btnToggleApps.setOnClickListener {
            if (isAppsViewVisible) {
                showDashboard()
            } else {
                showAppDrawer()
            }
        }

        // Map dock button
        binding.dockBar.btnDockMap.setOnClickListener {
            if (isAppsViewVisible) {
                showDashboard()
            }
        }

        // Spotify dock button
        binding.dockBar.btnDockSpotify.setOnClickListener {
            MediaControllerManager.openSpotify(this)
        }

        // Car settings button
        binding.dockBar.btnDockSettings.setOnClickListener {
            AppLauncherHelper.openSystemSettings(this)
        }

        // Google Assistant / Voice Search
        binding.dockBar.btnAssistant.setOnClickListener {
            AppLauncherHelper.launchVoiceAssistant(this)
        }

        // Volume HUD button
        binding.dockBar.btnVolume.setOnClickListener {
            AppLauncherHelper.openVolumePanel(this)
        }
    }

    fun showDashboard() {
        isAppsViewVisible = false
        binding.dockBar.ivLauncherIcon.setImageResource(R.drawable.ic_dashboard_apps)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, DashboardFragment())
            .commit()
    }

    fun showAppDrawer() {
        isAppsViewVisible = true
        binding.dockBar.ivLauncherIcon.setImageResource(R.drawable.ic_map)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, AppsFragment())
            .commit()
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If on app drawer, pressing back returns to dashboard
                if (isAppsViewVisible) {
                    showDashboard()
                }
                // Do not close app if already on dashboard (it's the home launcher!)
            }
        })
    }

    private fun startClock() {
        updateClock()
        clockHandler.removeCallbacks(clockRunnable)
        clockHandler.post(clockRunnable)
    }

    private fun stopClock() {
        clockHandler.removeCallbacks(clockRunnable)
    }

    private fun updateClock() {
        val currentTime = clockFormat.format(Date())
        binding.dockBar.tvDockClock.text = currentTime
    }

    private fun checkPermissions() {
        // 1. Check Location Permission
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            LocationSpeedManager.startListening(this)
        }

        // 2. Check Notification Listener Permission for Spotify Control
        if (!isNotificationServiceEnabled()) {
            promptNotificationAccess()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, CarMediaListenerService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun promptNotificationAccess() {
        AlertDialog.Builder(this, R.style.Theme_AutoCarLauncher)
            .setTitle(R.string.permission_notification_title)
            .setMessage(R.string.permission_notification_desc)
            .setPositiveButton("Configurar") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Acesse Configurações > Acesso a Notificações", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Depois", null)
            .show()
    }
}
