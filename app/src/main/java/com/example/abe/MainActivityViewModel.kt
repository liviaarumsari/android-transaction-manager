package com.example.abe

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abe.data.TransactionRepository
import com.example.abe.domain.FormatCurrencyUseCase
import com.example.abe.domain.GenerateExcelUseCase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivityViewModel(private val transactionRepository: TransactionRepository): ViewModel() {
    var newExcelFormat: Boolean = false

    fun getExportFileName(): String {
        val date = SimpleDateFormat("yyyy-MM-dd_hh-mm-ss" , Locale.ENGLISH).format(Date())
        return "Daftar-Transaksi_$date"
    }

    suspend fun exportTransactionsToExcel(contentResolver: ContentResolver, uri: Uri, user: String) {
        val headerList = listOf("ID Transaksi", "Email", "Judul", "Nominal", "Pengeluaran", "Waktu Transasksi")
        val transactions = transactionRepository.getAll(user)

        val dataList = mutableListOf<List<String>>()
        val currencyFormatter = FormatCurrencyUseCase()

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

        val generateExcel = GenerateExcelUseCase(newExcelFormat, contentResolver,  uri, "Transaksi", headerList, dataList)
        generateExcel()
    }

    suspend fun createEmailIntent(context: Context, user: String): Intent {
        clearExportCacheFiles(context)
        val newFile = File(context.cacheDir, if (newExcelFormat) "export.xlsx" else "export.xls")
        val contentUri =
            FileProvider.getUriForFile(context, "com.example.abe.fileprovider", newFile)
        exportTransactionsToExcel(context.contentResolver, contentUri, user)

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf("13521134@std.stei.itb.ac.id"))
            putExtra(Intent.EXTRA_SUBJECT, "test subject")

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }

        return intent
    }

    fun clearExportCacheFiles(context: Context) {
        context.cacheDir?.apply {
            val files = listFiles() ?: emptyArray()
            files.forEach { file ->
                if (file.name.startsWith("export"))
                    file.delete()
            }
        }
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