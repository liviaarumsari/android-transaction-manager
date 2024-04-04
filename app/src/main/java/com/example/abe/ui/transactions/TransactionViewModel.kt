package com.example.abe.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abe.data.Transaction
import com.example.abe.data.TransactionRepository


class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    fun getAllTransactions(user: String): LiveData<List<Transaction>> {
        return transactionRepository.getAll(user)
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}