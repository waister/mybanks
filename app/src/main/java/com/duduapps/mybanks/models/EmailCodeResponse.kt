package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class EmailCodeResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("identifier")
    val identifier: String? = null,
    @SerializedName("verifier")
    val verifier: String? = null,
)
