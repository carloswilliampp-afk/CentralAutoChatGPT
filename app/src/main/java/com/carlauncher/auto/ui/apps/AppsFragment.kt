package com.carlauncher.auto.ui.apps

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.carlauncher.auto.MainActivity
import com.carlauncher.auto.databinding.FragmentAppsBinding
import com.carlauncher.auto.model.AppItem
import com.carlauncher.auto.utils.AppLauncherHelper
import kotlinx.coroutines.launch

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private var allApps: List<AppItem> = emptyList()
    private lateinit var adapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadInstalledApps()

        binding.btnBackDashboard.setOnClickListener {
            (activity as? MainActivity)?.showDashboard()
        }
    }

    private fun setupRecyclerView() {
        adapter = AppsAdapter(emptyList()) { selectedApp ->
            AppLauncherHelper.launchApp(
                requireContext(),
                selectedApp.packageName,
                selectedApp.activityName
            )
        }

        // 5 columns is optimal for 1920x1080 7-inch automotive screen
        binding.rvAppsGrid.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvAppsGrid.adapter = adapter
    }

    private fun loadInstalledApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            allApps = AppLauncherHelper.getInstalledApps(requireContext())
            adapter.updateApps(allApps)
            updateEmptyState(allApps.isEmpty())
        }
    }

    private fun setupSearch() {
        binding.etSearchApps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                filterApps(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
        adapter.updateApps(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyApps.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
