package com.carlauncher.auto.ui.maps

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.carlauncher.auto.databinding.DialogDestinationSearchBinding
import com.carlauncher.auto.utils.AppLauncherHelper

class DestinationSearchDialog(
    context: Context,
    private val onSearchOnMap: (String) -> Unit
) : Dialog(context) {

    private val binding: DialogDestinationSearchBinding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogDestinationSearchBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        setupActions()
    }

    private fun setupActions() {
        binding.btnDialogClose.setOnClickListener {
            dismiss()
        }

        binding.btnDialogMaps.setOnClickListener {
            val query = binding.etDialogDestination.text.toString().trim()
            AppLauncherHelper.launchGoogleMaps(context, query.ifEmpty { null })
            dismiss()
        }

        binding.btnDialogWaze.setOnClickListener {
            val query = binding.etDialogDestination.text.toString().trim()
            AppLauncherHelper.launchWaze(context, query.ifEmpty { null })
            dismiss()
        }

        // Quick shortcut chips
        binding.chipGas.setOnClickListener {
            binding.etDialogDestination.setText("Posto de gasolina mais próximo")
        }

        binding.chipFood.setOnClickListener {
            binding.etDialogDestination.setText("Restaurante")
        }

        binding.chipParking.setOnClickListener {
            binding.etDialogDestination.setText("Estacionamento")
        }
    }
}
