package com.example.abe.ui.transactions

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.abe.data.Transaction
import com.example.abe.data.TransactionDAO
import com.example.abe.data.TransactionDatabase
import com.example.abe.data.TransactionRepository
import com.example.abe.domain.FormatCurrencyUseCase
import com.example.abe.domain.GenerateExcelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    val allTransactions = transactionRepository.allTransaction

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        transactionRepository.insert(transaction)
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}