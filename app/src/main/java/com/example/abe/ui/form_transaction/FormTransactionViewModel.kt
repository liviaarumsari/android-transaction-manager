package com.example.abe.ui.form_transaction

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.abe.data.Transaction
import com.example.abe.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class FormTransactionViewModel(private val transactionRepository: TransactionRepository) :
    ViewModel() {
    val title = MutableLiveData<String>("")

    val amount = MutableLiveData<String>("")
    val amountNumber = amount.map { it.toIntOrNull() }

    val category = MutableLiveData<String>("")
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val location = MutableLiveData<String>("")

    fun insertTransaction(user: String) = viewModelScope.launch(Dispatchers.IO) {
        if (amountNumber.value != null &&
            latitude.value != null && longitude.value != null &&
            !location.value.isNullOrEmpty() && title.value != null
        ) {
            val transaction = Transaction(
                id = 0,
                email = user,
                title = title.value!!,
                amount = amountNumber.value!!,
                isExpense = category.value == "Expenses",
                timestamp = Date(),
                latitude = latitude.value!!,
                longitude = longitude.value!!,
                location = location.value!!,
            )
            transactionRepository.insert(transaction)
        }
    }

    fun getTransaction(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = transactionRepository.getById(id)
        withContext(Dispatchers.Main) {
            title.value = transaction.title
            amount.value = transaction.amount.toString()
            category.value = if (transaction.isExpense) "Expense" else "Income"
            latitude.value = transaction.latitude
            longitude.value = transaction.longitude
            location.value = transaction.location
        }
    }

    fun updateTransaction(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        val oldTransaction = transactionRepository.getById(id)
        val newTransaction = Transaction(
            id = id,
            email = oldTransaction.email,
            title = title.value!!,
            amount = amountNumber.value!!,
            isExpense = oldTransaction.isExpense,
            timestamp = oldTransaction.timestamp,
            latitude = latitude.value!!,
            longitude = longitude.value!!,
            location = location.value!!,
        )
        transactionRepository.update(newTransaction)
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        transactionRepository.deleteById(id)
    }

    fun setRandomAmount(randomAmount: Int) {
        amount.value = randomAmount.toString()
    }
}

class FormTransactionViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FormTransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}