package com.example.abe.ui.add_transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.abe.data.Transaction
import com.example.abe.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class FormTransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {
    val title = MutableLiveData<String>("")
    val amount = MutableLiveData<Int>()
    val category = MutableLiveData<String>("")
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val location = MutableLiveData<String>("")

    fun insertTransaction() = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            id = 0,
            email = "example@example.com",
            title = title.value!!,
            amount = amount.value!!,
            isExpense = category.value == "Expense",
            timestamp = Date(),
            latitude = latitude.value!!,
            longitude = longitude.value!!,
            location = location.value!!,
        )
        transactionRepository.insert(transaction)
    }
}

class FormTransactionViewModelFactory(private val repository: TransactionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FormTransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}