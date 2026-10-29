package com.carlauncher.auto.ui.maps

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carlauncher.auto.R
import com.carlauncher.auto.databinding.FragmentMapWidgetBinding
import com.carlauncher.auto.utils.AppLauncherHelper
import com.carlauncher.auto.utils.LocationSpeedManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

class MapWidgetFragment : Fragment() {

    private var _binding: FragmentMapWidgetBinding? = null
    private val binding get() = _binding!!

    private var carMarker: Marker? = null
    private var isUserInteractingWithMap = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMapView()
        setupListeners()
        observeGpsAndSpeed()
    }

    private fun setupMapView() {
        binding.osmMapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            // Apply Automotive Dark Mode filter to map tiles for Android Auto aesthetic
            val darkMatrix = ColorMatrix().apply {
                // Invert colors and tone down brightness
                set(floatArrayOf(
                    -0.85f, 0f, 0f, 0f, 220f,
                    0f, -0.85f, 0f, 0f, 220f,
                    0f, 0f, -0.85f, 0f, 220f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(darkMatrix))

            // Default initial zoom (Automotive street-level navigation)
            controller.setZoom(16.5)
            // Default center if GPS not yet acquired (Brazil center / São Paulo default)
            val defaultPoint = GeoPoint(-23.550520, -46.633308)
            controller.setCenter(defaultPoint)
        }

        // Setup Car Marker
        carMarker = Marker(binding.osmMapView).apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.car_nav_arrow)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Veículo"
        }
        binding.osmMapView.overlays.add(carMarker)
    }

    private fun setupListeners() {
        // Destination Search Bar
        binding.barSearchDestination.setOnClickListener {
            DestinationSearchDialog(requireContext()) { query ->
                // Search or route on map
            }.show()
        }

        // Voice button
        binding.btnMapVoice.setOnClickListener {
            AppLauncherHelper.launchVoiceAssistant(requireContext())
        }

        // Direct Google Maps launch
        binding.btnLaunchGoogleMaps.setOnClickListener {
            AppLauncherHelper.launchGoogleMaps(requireContext())
        }

        // Direct Waze launch
        binding.btnLaunchWaze.setOnClickListener {
            AppLauncherHelper.launchWaze(requireContext())
        }

        // Recenter on car GPS
        binding.btnMapRecenter.setOnClickListener {
            val location = LocationSpeedManager.currentLocation.value
            if (location != null) {
                val point = GeoPoint(location.latitude, location.longitude)
                binding.osmMapView.controller.animateTo(point)
                isUserInteractingWithMap = false
            }
        }
    }

    private fun observeGpsAndSpeed() {
        // Observe vehicle speed (km/h)
        viewLifecycleOwner.lifecycleScope.launch {
            LocationSpeedManager.currentSpeedKmH.collectLatest { speed ->
                binding.tvCarSpeed.text = speed.toString()
            }
        }

        // Observe vehicle location
        viewLifecycleOwner.lifecycleScope.launch {
            LocationSpeedManager.currentLocation.collectLatest { location ->
                if (location != null) {
                    updateCarLocationOnMap(location)
                }
            }
        }
    }

    private fun updateCarLocationOnMap(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        carMarker?.apply {
            position = geoPoint
            if (location.hasBearing()) {
                rotation = location.bearing
            }
        }

        if (!isUserInteractingWithMap) {
            binding.osmMapView.controller.setCenter(geoPoint)
        }

        binding.osmMapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.osmMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.osmMapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
