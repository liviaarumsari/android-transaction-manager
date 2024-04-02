package com.example.abe.ui.transactions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.ABEApplication
import com.example.abe.databinding.FragmentTransactionsBinding
import com.example.abe.types.FragmentListener


class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private lateinit var listener: ItemClickListener

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    interface ItemClickListener {
        fun onItemClicked(id: Int)
    }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory((activity?.application as ABEApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        val transactionsAdapter = TransactionsAdapter(listener, requireContext())
        binding.rvTransactions.adapter = transactionsAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)

//        TODO: check only for transactions by current user
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

        binding.fabRandom.setOnClickListener {
            Intent().also { intent ->
                intent.setAction("RANDOMIZE_TRANSACTION")
                intent.putExtra("random_amount", (10000..100000).random())
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
        }

        binding.addTransactionBtn.setOnClickListener {
            (activity as FragmentListener).onIntentReceived("OPEN_FORM", "")
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemClickListener) {
            listener = context
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}