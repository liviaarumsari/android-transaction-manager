package com.example.abe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Upsert

@Dao
interface TransactionDAO {

    @Upsert
    fun upsertTransaction(transaction: Transaction)


}