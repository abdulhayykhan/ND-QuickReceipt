package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT COUNT(id) FROM templates")
    suspend fun getTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Query("UPDATE templates SET isSelected = 0")
    suspend fun deselectAll()

    @Query("UPDATE templates SET isSelected = 1 WHERE id = :id")
    suspend fun selectTemplateById(id: Int)
    
    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Int)
}
