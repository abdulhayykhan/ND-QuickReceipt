package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class ReceiptRepository(private val receiptDao: ReceiptDao, private val templateDao: TemplateDao) {
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
        .catch { e -> 
            e.printStackTrace()
            emit(emptyList()) 
        }
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()
        .catch { e -> 
            e.printStackTrace()
            emit(emptyList()) 
        }

    suspend fun insert(receipt: ReceiptEntity) = receiptDao.insertReceipt(receipt)

    suspend fun deleteById(id: Int) = receiptDao.deleteReceiptById(id)
    
    suspend fun getTemplateCount(): Int = templateDao.getTemplateCount()
    
    suspend fun insertTemplate(template: TemplateEntity) = templateDao.insertTemplate(template)
    
    suspend fun selectTemplate(id: Int) {
        templateDao.deselectAll()
        templateDao.selectTemplateById(id)
    }
    
    suspend fun deleteTemplateById(id: Int) = templateDao.deleteTemplateById(id)
}
