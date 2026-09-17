package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class IdentifyResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("version_last")
    val versionLast: Int = 0,
    @SerializedName("version_min")
    val versionMin: Int = 0,
)
