package com.example.abe.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.ABEApplication
import com.example.abe.databinding.FragmentTransactionsBinding


class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TransactionFragment()
    }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory((activity?.application as ABEApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        val transactionsAdapter = TransactionsAdapter()
        binding.rvTransactions.adapter = transactionsAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactions?.let {
                transactionsAdapter.submitList(it)
            }
        }

        binding.fabExport.setOnClickListener {
            ExportAlertDialogFragment.newInstance(ExportAlertDialogTypeEnum.EXPORT)
                .show(requireActivity().supportFragmentManager, "EXPORT_DIALOG")
        }

        binding.fabEmail.setOnClickListener {
            ExportAlertDialogFragment.newInstance(ExportAlertDialogTypeEnum.SEND_EMAIL)
                .show(requireActivity().supportFragmentManager, "EXPORT_DIALOG")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}