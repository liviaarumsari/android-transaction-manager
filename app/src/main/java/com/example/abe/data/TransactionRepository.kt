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

    @WorkerThread
    suspend fun getExpenseTotalAmount(isExpense: Boolean): Int {
//        TODO: check only for transactions by current user
        return transactionDAO.getExpenseTotalAmount(isExpense)
    }
}