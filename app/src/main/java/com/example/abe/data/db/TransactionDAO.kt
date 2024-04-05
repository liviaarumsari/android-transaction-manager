package com.example.abe.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDAO {

    @Insert
    suspend fun insert(vararg transaction: Transaction)

    @Update
    suspend fun update(vararg transaction: Transaction)

    @Delete
    suspend fun delete(vararg transactions: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions WHERE email = :email ORDER BY timestamp DESC")
    fun getAllObservable(vararg email: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE email = :email ORDER BY timestamp DESC")
    suspend fun getAll(vararg email: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isExpense = :isExpense AND email = :email")
    suspend fun getExpenseTotalAmount(isExpense: Boolean, email: String): Int
}