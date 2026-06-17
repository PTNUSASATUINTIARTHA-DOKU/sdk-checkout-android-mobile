package com.doku.sdkcheckoutandroid.model.response

data class ShowPromosResponse(
    val promos: List<Promo>?
)

data class Promo(
    val title: String?,
    val code: String?,
    val end_date_time: String?,
    val description: String?,
    val channels: List<String>?,
    val tnc: Tnc?
)

data class PromoModel(
    val title: String,
    val desc: String,
    val badge: String,
    val code: String,
    val expiry: String
)

data class Tnc(
    val type: String?,
    val value: String?
)
