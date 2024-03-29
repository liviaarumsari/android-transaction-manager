package com.example.abe.ui.transactions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abe.data.TransactionRepository
import com.example.abe.domain.FormatCurrencyUseCase
import com.example.abe.domain.GenerateExcelUseCase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    val allTransactions = transactionRepository.allTransaction

    fun generateExcelInCache(context: Context): Uri {
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

        val newFile = File(context.externalCacheDir, "export.xlsx")
        val contentUri =
            FileProvider.getUriForFile(context, "com.example.abe.fileprovider", newFile)
        val generateExcel = GenerateExcelUseCase(true, context.contentResolver,  contentUri, "Transaksi", headerList, dataList)
        generateExcel()

        return contentUri
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