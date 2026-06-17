package com.doku.sdkcheckoutandroid.model.request

import com.doku.sdkcheckoutandroid.model.response.Installment
import java.io.Serializable

data class PaymentRequest (
    val token_id: String,
    val bank: String? = null,
    val callback_url: String? = null,
    val customer_phone: String? = null,
    val retry_payment: Boolean? = null,
    val acquirer_name: String? = null,
    val doku_id: String? = null,
    val request_id: String? = null,
    val client_id: String? = null,
    val data: String? = null,
    val channel_id: String? = null,
    val installment: InstallmentRequest? = null
): Serializable