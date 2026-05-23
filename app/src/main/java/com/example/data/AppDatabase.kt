package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReceiptEntity::class, TemplateEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun templateDao(): TemplateDao
}
