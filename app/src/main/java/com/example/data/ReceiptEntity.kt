package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val serviceDetails: String,
    val amount: String,
    val timestamp: Long = System.currentTimeMillis()
)
