package com.doku.sdkcheckoutandroid.model.response

import java.io.Serializable

data class CardTokenizeResponse(
    val customer: Customer,
    val credit_card: CreditCard
): Serializable

data class Customer(
    val id: String
): Serializable

data class CreditCard(
    val cardholder_name: String?,
    val issuer: String?,
    val issuer_image: String?,
    val token_id: String,
    val masked_card: String,
    val brand: String,
    val brand_image: String?,
    val bin: String,
    val expiry: String,
    val payer_account_id: String
): Serializable