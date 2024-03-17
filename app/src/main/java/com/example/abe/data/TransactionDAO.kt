package com.example.abe.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDAO {

    @Insert
    suspend fun insert(transaction: Transaction)
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions")
    fun getAll(): LiveData<List<Transaction>>
}