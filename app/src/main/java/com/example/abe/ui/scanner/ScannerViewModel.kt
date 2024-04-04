package com.example.abe.ui.scanner

import androidx.lifecycle.ViewModel
import com.example.abe.data.TransactionRepository
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.abe.data.Transaction
import com.example.abe.data.network.TransactionItem
import java.util.Date

class ScannerViewModel(private val transactionRepository: TransactionRepository):
    ViewModel() {
    fun insertTransaction(user: String, item: TransactionItem, lat: Double, long: Double) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            id = 0,
            email = user,
            title = item.name,
            amount = (item.qty * item.price).toInt(),
            isExpense = (item.qty * item.price).toInt() < 0,
            timestamp = Date(),
            latitude = lat,
            longitude = long,
            location = "location",
            )
        transactionRepository.insert(transaction)
        }
}

class ScannerViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}
