package com.example.abe.data.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class TransactionRepository(private val transactionDAO: TransactionDAO) {

    fun getAllObservable(email: String): LiveData<List<Transaction>> {
        return transactionDAO.getAllObservable(email)
    }

    @WorkerThread
    suspend fun getAll(email: String): List<Transaction> {
        return transactionDAO.getAll(email)
    }

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
    suspend fun getExpenseTotalAmount(isExpense: Boolean, email: String): Int {
        return transactionDAO.getExpenseTotalAmount(isExpense, email)
    }
}