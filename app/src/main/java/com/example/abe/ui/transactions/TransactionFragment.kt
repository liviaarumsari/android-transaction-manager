package com.example.abe.ui.transactions

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.R
import com.example.abe.data.Transaction
import com.example.abe.databinding.FragmentTransactionsBinding
import java.util.Date

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TransactionFragment()
    }

    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transactions = mutableListOf<Transaction>(
            Transaction(1, "a@gmail.com", "food", 1000, false, Date()),
            Transaction(2, "a@gmail.com", "food", 2000, false, Date()),
            Transaction(3, "a@gmail.com", "food", 3000, true, Date()),
            Transaction(4, "a@gmail.com", "food", 4000, false, Date()),
            Transaction(5, "a@gmail.com", "food", 5000, false, Date())
        )
        val transactionsAdapter = TransactionsAdapter(transactions)

        binding.rvTransactions.adapter = transactionsAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}