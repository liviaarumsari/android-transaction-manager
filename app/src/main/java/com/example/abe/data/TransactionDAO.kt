package com.example.abe.data

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

    @Query("SELECT * FROM transactions")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

//    TODO: check only for transactions by current user
    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = :isExpense")
    suspend fun getExpenseTotalAmount(isExpense: Boolean): Int
}