package com.doku.sdkcheckoutandroid.helper

import android.content.Context

object DokuConfig {
    lateinit var appContext: Context

    lateinit var baseUrl: String

    lateinit var checkoutExpiryTime: String

    lateinit var clientId: String
    lateinit var requestTimestamp: String
    lateinit var  requestId: String
    lateinit var invoiceNumber: String
    lateinit var signature: String

    var colorPallete: String? = null
        set(value) {
            field = try {
                if (value.isNullOrBlank()) null
                else {
                    android.graphics.Color.parseColor(value)
                    value // valid → simpan
                }
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("DOKU_CONFIG", "Invalid color palette: $value")
                null // invalid → auto null
            }
        }

    val nextPhasePayment = listOf("DIRECT_DEBIT", "PEER_TO_PEER", "CREDIT_CARD_CPTS", "DIGITAL_BANK", "DIGITAL_BANKING", "INTERNET_BANKING")

    fun initDb(context: Context) {
        appContext = context.applicationContext
    }

    fun setEnvironment(production: Boolean) {
        if(production) {
            baseUrl = "https://app.doku.com"
        } else {
            baseUrl = "https://api-sandbox.doku.com"
        }
    }

    fun setExpiryTime(expiry: String) {
        checkoutExpiryTime = expiry
    }

    fun setMerchantConfig(clientId: String, requestTimestamp: String,requestId: String,
                          invoiceNumber: String, signature: String){
        DokuConfig.clientId = clientId
        DokuConfig.requestTimestamp = requestTimestamp
        DokuConfig.requestId = requestId
        DokuConfig.invoiceNumber = invoiceNumber
        DokuConfig.signature = signature
    }
}