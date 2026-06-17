package com.doku.sdkcheckoutandroid.model.response

import java.io.Serializable

data class ShowPaymentMethodResponse(
    val client_id: String?,
    val customer_id: String?,
    val customer_name: String?,
    val customer_email: String?,
    val customer_phone: String?,
    val customer_address: String?,
    val customer_area_code: String?,
    val currency: String?,
    val amount: Int?,
    val invoice_number: String?,
    val payment_method_type: List<PaymentMethodType>?,
    val abandoned_cart_recovery: AbandonedCartRecovery?,
    val sorting_status: Boolean?,
    val callback_url: String?,
    val token_id: String?,
    val status: String?,
    val order_items: List<OrderItem>?,
    val timeout: String?,
    val client_name: String?,
    val tracking_id_doku: String?,
    val tracking_id_merchant: String?,
    val tracking_property_name: String?,
    val color_configuration: ColorConfiguration?,
    val language: String?,
    val auto_redirect: Boolean?,
    val disable_retry_payment: Boolean?,
    val close_redirect_url: String?,
    val request_id: String?,
    val is_cc_h2h: Boolean?,
    val recurring: Recurring?,
    val data_promo: DataPromo?,
    val disclaimer: Disclaimer?,
    val hide_expiry_time: Boolean?,
    val allow_save: Boolean?,
    val name_on_card: Boolean?,
    val show_installment: Boolean?,
    val brand: List<Brand>?,
    val collect_customer: CollectCustomer?,
    val channel_selected: List<String>?,
    val last_used_payment_channel: LastUsedPaymentChannel?,
    val business_country: String?,
    val transaction_status: String?,
    val state: String?,
    val type: String?,
    val business: Business?
): Serializable

data class PaymentMethodType(
    val channel_category_sequence: Int?,
    val category_name: String?,
    val detail: List<PaymentDetail>?
): Serializable

data class PaymentDetail(
    val payment_channel_id: String?,
    val name: String?,
    val status: String?,
    val channel_sequence: Int?,
    val enable_payment: List<String>?
): Serializable

data class AbandonedCartRecovery(
    val max_retry_count: Int?,
    val abandoned_retry_count: Int?
): Serializable

data class OrderItem(
    val name: String?,
    val quantity: Int?,
    val price: String?,
    val sku: String?,
    val category: String?
): Serializable

data class ColorConfiguration(
    val main_page_background: String?,
    val header_font: String?,
    val header_background: String?,
    val countdown_font: String?,
    val countdown_background: String?,
    val footer_font: String?,
    val card_title_font: String?,
    val card_primary_font: String?,
    val card_secondary_font: String?,
    val icon: String?,
    val button_font: String?,
    val button_background: String?,
    val card_background_color: String?,
    val footer_background: String?,
    val amount_to_pay: String?,
    val logo: String?,
    val color_type: String?
): Serializable

data class Recurring(
    val billing: Billing?,
    val iteration: Iteration?
): Serializable

data class Billing(
    val number: String?,
    val type: String?,
    val description: String?
): Serializable

data class Iteration(
    val type: String?,
    val start_date: String?,
    val end_date: String?,
    val schedule: Schedule?
): Serializable

data class Schedule(
    val days_of_week: List<String>?,
    val days_of_month: List<Int>?,
    val months: List<Int>?,
    val custom: List<String>?
): Serializable

data class DataPromo(
    val amount_discount: Int?,
    val amount_paid: Int?,
    val promo_channel_selected: String?,
    val promo_channel_applied: List<String>?,
    val code: String?,
    val is_merchant_promo_code: Boolean?,
    val is_merchant_promo_code_valid: Boolean?,
    val is_promo_expired: Boolean?,
    val type: String?
): Serializable

data class Disclaimer(
    val en: String?,
    val id: String?
): Serializable

data class Brand(
    val name: String?,
    val image_url: String?
): Serializable

data class CollectCustomer(
    val name: Boolean?,
    val phone: Boolean?,
    val email: Boolean?,
    val address: Boolean?
): Serializable

data class LastUsedPaymentChannel(
    val category_name: String?,
    val enabled: Boolean?,
    val detail: PaymentChannelDetail?
): Serializable

data class PaymentChannelDetail(
    val payment_channel_id: String?,
    val payment_code: String?,
    val status: String?
): Serializable

data class Business(
    val id: String?,
    val name: String?,
    val email: String?,
    val phone_no: String?,
    val license_id: String?
): Serializable