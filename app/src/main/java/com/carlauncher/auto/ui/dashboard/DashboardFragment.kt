package com.carlauncher.auto.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.carlauncher.auto.R
import com.carlauncher.auto.databinding.FragmentDashboardBinding
import com.carlauncher.auto.ui.maps.MapWidgetFragment
import com.carlauncher.auto.ui.media.MediaWidgetFragment
import com.carlauncher.auto.utils.AppLauncherHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChildFragments()
        setupQuickGlanceInfo()
    }

    private fun setupChildFragments() {
        if (childFragmentManager.findFragmentById(R.id.map_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_container, MapWidgetFragment())
                .commit()
        }

        if (childFragmentManager.findFragmentById(R.id.media_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.media_container, MediaWidgetFragment())
                .commit()
        }
    }

    private fun setupQuickGlanceInfo() {
        // Date formatting in Portuguese (e.g. "Quarta-feira, 28 de Outubro")
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val formattedDate = dateFormat.format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }

        binding.tvGlanceDate.text = formattedDate

        // Quick destination shortcuts
        binding.btnQuickHome.setOnClickListener {
            AppLauncherHelper.launchGoogleMaps(requireContext(), "Casa")
        }

        binding.btnQuickWaze.setOnClickListener {
            AppLauncherHelper.launchWaze(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
