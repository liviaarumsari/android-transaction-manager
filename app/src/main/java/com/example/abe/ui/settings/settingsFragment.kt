package com.example.abe.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.abe.databinding.FragmentSettingsBinding
import com.example.abe.ui.transactions.ExportAlertDialogFragment
import com.example.abe.ui.transactions.ExportAlertDialogTypeEnum

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.clSave.setOnClickListener {
            ExportAlertDialogFragment.newInstance(ExportAlertDialogTypeEnum.EXPORT)
                .show(requireActivity().supportFragmentManager, "EXPORT_DIALOG")
        }

        binding.clSend.setOnClickListener {
            ExportAlertDialogFragment.newInstance(ExportAlertDialogTypeEnum.SEND_EMAIL)
                .show(requireActivity().supportFragmentManager, "EXPORT_DIALOG")
        }

        binding.clRandomize.setOnClickListener {
            Intent().also { intent ->
                intent.setAction("RANDOMIZE_TRANSACTION")
                intent.putExtra("random_amount", (10000..100000).random())
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
        }

        binding.clLogout.setOnClickListener {
            Intent().also { intent ->
                intent.setAction("EXPIRED_TOKEN")
                LocalBroadcastManager.getInstance(requireActivity().applicationContext).sendBroadcast(intent)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}