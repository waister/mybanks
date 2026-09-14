package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class BaseApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
)
