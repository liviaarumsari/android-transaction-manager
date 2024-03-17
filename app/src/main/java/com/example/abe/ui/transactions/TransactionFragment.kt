package com.example.abe.ui.transactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abe.ABEApplication
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

//        val transactions = mutableListOf<Transaction>(
//            Transaction(1, "a@gmail.com", "food", 1000, false, Date()),
//            Transaction(2, "a@gmail.com", "food", 2000, false, Date()),
//            Transaction(3, "a@gmail.com", "food", 3000, true, Date()),
//            Transaction(4, "a@gmail.com", "food", 4000, false, Date()),
//            Transaction(5, "a@gmail.com", "food", 5000, false, Date())
//        )
        val transactionsAdapter = TransactionsAdapter()
        binding.rvTransactions.adapter = transactionsAdapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)

        viewModel.allTransactions.observe(viewLifecycleOwner) {transactions ->
            transactions?.let {
                Log.d("ABE-TRX", it.toString())
                transactionsAdapter.submitList(it)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}