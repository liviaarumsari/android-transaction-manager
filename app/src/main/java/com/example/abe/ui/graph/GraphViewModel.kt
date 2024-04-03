package com.example.abe.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abe.data.TransactionRepository

class GraphViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {
    suspend fun getExpenses(user: String): Int {
        return transactionRepository.getExpenseTotalAmount(true, user)
    }

    suspend fun getIncome(user: String): Int {
        return transactionRepository.getExpenseTotalAmount(false, user)
    }
}

class GraphViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GraphViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}