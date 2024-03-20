package com.example.abe.ui.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.ABEApplication
import com.example.abe.data.Transaction
import com.example.abe.databinding.FragmentTransactionsBinding
import java.util.Date
import java.util.UUID

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val CREATE_FILE = 1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TransactionFragment()
    }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory((activity?.application as ABEApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        val transactionsAdapter = TransactionsAdapter()
        binding.rvTransactions.adapter = transactionsAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)

        viewModel.allTransactions.observe(viewLifecycleOwner) {transactions ->
            transactions?.let {
                transactionsAdapter.submitList(it)
            }
        }

        binding.fabExport.setOnClickListener {
            ExportAlertDialogFragment().show(requireActivity().supportFragmentManager, "EXPORT_DIALOG")
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}