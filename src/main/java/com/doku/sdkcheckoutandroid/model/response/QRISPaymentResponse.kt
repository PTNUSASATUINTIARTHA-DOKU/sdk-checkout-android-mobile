package com.doku.sdkcheckoutandroid.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QRISPaymentResponse(
    @SerializedName("qr_code")
    val qrCode: String?,

    @SerializedName("nmid")
    val nmid: String?,

    @SerializedName("systrace")
    val systrace: Int?,

    @SerializedName("access_token")
    val accessToken: String?
): Serializable