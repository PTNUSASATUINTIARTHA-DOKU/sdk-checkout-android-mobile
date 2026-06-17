package com.doku.sdkcheckoutandroid.model.request

import java.io.Serializable

data class CheckoutRequest(
    val order: Order,
    val payment: Payment? = null,
    val customer: Customer? = null,
    val shipping_address: Address ? = null,
    val billing_address: Address ? = null,
    val additional_info: AdditionalInfo? = null,
    var origin: Origin? = null
): Serializable

data class Order(
    val amount: Int,
    val invoice_number: String,
    val currency: String,
    val callback_url: String? = null,
    val callback_url_cancel: String? = null,
    val callback_url_result: String? = null,
    val language: String,
    val auto_redirect: Boolean? = null,
    val disable_retry_payment: Boolean? = null,
    val recover_abandoned_cart: Boolean? = null,
    val expired_recovered_cart: Int? = null,
    val line_items: List<LineItem>? = null
): Serializable

data class LineItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val sku: String,
    val category: String,
    val url: String,
    val image_url: String,
    val type: String
): Serializable

data class Payment(
    val payment_due_date: Int,
    val type: String,
): Serializable

data class Customer(
    val id: String,
    val name: String,
    val last_name: String,
    val phone: String,
    val email: String,
    val address: String,
    val postcode: String,
    val state: String,
    val city: String,
    val country: String
): Serializable

data class Address(
    val first_name: String,
    val last_name: String,
    val address: String,
    val city: String,
    val postal_code: String,
    val phone: String,
    val country_code: String
): Serializable

data class AdditionalInfo(
    val allow_tenor: List<Int>,
    val doku_wallet_notify_url: String,
    val override_notification_url: String,
): Serializable

data class Origin(val product: String, val source: String, val system: String): Serializable