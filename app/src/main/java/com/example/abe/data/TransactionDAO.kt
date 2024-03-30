package com.example.abe.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDAO {

    @Insert
    suspend fun insert(vararg transaction: Transaction)

    @Delete
    suspend fun delete(vararg users: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int): Transaction
}