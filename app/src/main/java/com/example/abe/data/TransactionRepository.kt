package com.example.abe.data

import androidx.annotation.WorkerThread

class TransactionRepository(private val transactionDAO: TransactionDAO) {
    val allTransaction = transactionDAO.getAll()

    @WorkerThread
    suspend fun insert(transaction: Transaction) {
        transactionDAO.insert(transaction)
    }
}