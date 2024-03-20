package com.example.abe

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abe.data.TransactionRepository
import com.example.abe.domain.FormatCurrencyUseCase
import com.example.abe.domain.GenerateExcelUseCase
import com.example.abe.ui.transactions.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivityViewModel(private val transactionRepository: TransactionRepository): ViewModel() {
    var newExcelFormat: Boolean = false

    fun getExportFileName(): String {
        val date = SimpleDateFormat("yyyy-MM-dd_hh:mm:ss" , Locale.ENGLISH).format(Date())
        return "Daftar-Transaksi_$date"
    }

    fun exportTransactionsToExcel(contentResolver: ContentResolver, uri: Uri) {
        val headerList = listOf("ID Transaksi", "Email", "Judul", "Nominal", "Pengeluaran", "Waktu Transasksi")
        val transactions = transactionRepository.allTransaction.value
        val dataList = mutableListOf<List<String>>()
        val currencyFormatter = FormatCurrencyUseCase()

        if (transactions != null) {
            for (trx in transactions) {
                val rowData = listOf<String>(
                    trx.id.toString(),
                    trx.email,
                    trx.title,
                    currencyFormatter(trx.amount),
                    if (trx.isExpense) "Ya" else "Tidak",
                    SimpleDateFormat("d MMM yyyy" , Locale.ENGLISH).format(trx.timestamp)
                )
                dataList.add(rowData)
            }
        }

        val generateExcel = GenerateExcelUseCase(newExcelFormat, contentResolver,  uri, "Transaksi", headerList, dataList)
        generateExcel()
    }
}

class MainActivityViewModelFactory(private val repository: TransactionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}