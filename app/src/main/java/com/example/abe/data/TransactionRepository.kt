package com.example.abe.data

import androidx.annotation.WorkerThread

class TransactionRepository(private val transactionDAO: TransactionDAO) {
    val allTransaction = transactionDAO.getAll()

    @WorkerThread
    suspend fun getById(id: Int): Transaction {
        return transactionDAO.getById(id)
    }

    @WorkerThread
    suspend fun deleteById(id: Int) {
        transactionDAO.deleteById(id)
    }

    @WorkerThread
    suspend fun delete(vararg transaction: Transaction) {
        transactionDAO.delete(*transaction)
    }

    @WorkerThread
    suspend fun insert(vararg transaction: Transaction) {
        transactionDAO.insert(*transaction)
    }

    @WorkerThread
    suspend fun update(vararg transaction: Transaction) {
        transactionDAO.update(*transaction)
    }
}