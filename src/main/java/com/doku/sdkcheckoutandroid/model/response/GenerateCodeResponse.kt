package com.doku.sdkcheckoutandroid.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// MARK: - Root Response
data class GenerateCodeResponse(
    val message: String? = null,

    @SerializedName("payment_code")
    val paymentCode: String? = null,

    @SerializedName("how_to_pay_url")
    val howToPayURL: String? = null,

    @SerializedName("expired_date")
    val expiredDate: String? = null,

    @SerializedName("expired_date_utc")
    val expiredDateUTC: String? = null,

    @SerializedName("created_date")
    val createdDate: String? = null,

    @SerializedName("created_date_utc")
    val createdDateUTC: String? = null,

    @SerializedName("payment_instruction_idn")
    val paymentInstructionIDN: List<PaymentInstruction>? = null,

    @SerializedName("payment_instruction_en")
    val paymentInstructionEN: List<PaymentInstruction>? = null
): Serializable

// MARK: - Payment Instruction
data class PaymentInstruction(
    val channel: String? = null,
    val step: List<String>? = null
): Serializable
