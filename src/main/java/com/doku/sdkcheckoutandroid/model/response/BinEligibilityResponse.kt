package com.doku.sdkcheckoutandroid.model.response

import java.io.Serializable

data class BinEligibilityResponse(
    val bin_number: String? = null,
    val country_id: String? = null,
    val acquirer_name: String? = null,
    val acquirer_image: String? = null,
    val brand: String? = null,
    val brand_image: String? = null,
    val eligible_status: Boolean = false,
    val installments: List<Installment> = emptyList(),
    val allow_non_installment: Boolean = false
): Serializable

data class Installment(
    val id: Int = 0,
    val tenor: Int = 0
): Serializable {
    override fun toString(): String {
        if(tenor == 9999999) {
            return "Full Payment"
        }
        return "$tenor Month"
    }
}