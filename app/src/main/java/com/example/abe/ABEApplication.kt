package com.example.abe

import android.app.Application
import com.example.abe.data.TransactionDatabase
import com.example.abe.data.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ABEApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { TransactionDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TransactionRepository(database.transactionDAO()) }
}