package com.example.abe.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val email: String,
    val title: String,
    val amount: Int,
    val isExpense: Boolean,
    val timestamp: Date,
    val latitude: Double,
    val longitude: Double,
    val location: String,
)