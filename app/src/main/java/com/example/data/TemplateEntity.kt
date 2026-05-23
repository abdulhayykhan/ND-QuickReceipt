package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val headerTitle: String,
    val headerWebsite: String,
    val headerPhone: String,
    val headerEmail: String,
    val footerText: String,
    val paperSize: String,
    val isSelected: Boolean = false
)
