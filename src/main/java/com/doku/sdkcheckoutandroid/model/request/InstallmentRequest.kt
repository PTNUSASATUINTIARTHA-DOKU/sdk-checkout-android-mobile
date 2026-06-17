package com.doku.sdkcheckoutandroid.model.request

import java.io.Serializable

data class InstallmentRequest(
    val id: Int,
    val tenor: Int
): Serializable