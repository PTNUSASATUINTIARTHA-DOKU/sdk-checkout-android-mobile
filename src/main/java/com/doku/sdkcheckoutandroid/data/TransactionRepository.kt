package com.doku.sdkcheckoutandroid.data

import com.doku.sdkcheckoutandroid.dao.TransactionDao
import com.doku.sdkcheckoutandroid.entity.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun getByInvoiceNumber(invoice: String): List<Transaction>{
        return transactionDao.getByInvoiceNumber(invoice)
    }
}