package com.doku.sdkcheckoutandroid.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class JumpAppWalletResponse(

    @SerializedName("invoice_number")
    val invoiceNumber: String?,
    val amount: Int?,
    @SerializedName("expired_date")
    val expiredDate: String?,
    @SerializedName("redirect_url_http")
    val redirectUrlHttp: String?,
    val message: String?

): Serializable