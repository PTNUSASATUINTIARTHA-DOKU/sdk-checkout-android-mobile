package com.doku.sdkcheckoutandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doku.sdkcheckoutandroid.entity.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM `transaction` WHERE invoiceNumber = :invoice ORDER BY id DESC LIMIT 1")
    suspend fun getByInvoiceNumber(invoice: String): List<Transaction>
}