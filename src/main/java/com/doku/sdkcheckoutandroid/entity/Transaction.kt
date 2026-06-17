package com.doku.sdkcheckoutandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

@Entity(tableName = "transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceNumber: String,
    val tokenId: String,
    val paymentMethod: String,
    val amount: Int,
    val paymentList: String,
    val paymentDueDate: String,
    val acquirer: String
)