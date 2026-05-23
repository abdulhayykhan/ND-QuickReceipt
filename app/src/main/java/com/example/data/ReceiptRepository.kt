package com.example.data

import kotlinx.coroutines.flow.Flow

class ReceiptRepository(private val receiptDao: ReceiptDao, private val templateDao: TemplateDao) {
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()

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
