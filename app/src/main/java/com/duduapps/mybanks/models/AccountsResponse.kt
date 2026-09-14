package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class AccountsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("accounts")
    val accounts: List<Account> = emptyList(),
)
