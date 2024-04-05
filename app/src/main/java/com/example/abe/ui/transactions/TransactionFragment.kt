package com.example.abe.ui.transactions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.ABEApplication
import com.example.abe.MainActivity
import com.example.abe.data.local.PreferenceDataStoreConstants
import com.example.abe.databinding.FragmentTransactionsBinding
import com.example.abe.types.FragmentListener
import kotlinx.coroutines.launch


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

        lifecycleScope.launch {
            val user =  (activity as MainActivity).preferenceDataStoreHelper.getFirstPreference(PreferenceDataStoreConstants.USER,"")
            viewModel.getAllTransactions(user).observe(viewLifecycleOwner) { transactions ->
                transactions?.let {
                    transactionsAdapter.submitList(it)
                }
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