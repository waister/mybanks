package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class Account(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("pix_code")
    val pixCode: String = "",
    @SerializedName("bank_id")
    val bankId: Int = 0,
    @SerializedName("label")
    val label: String = "",
    @SerializedName("agency")
    val agency: String = "",
    @SerializedName("account")
    val account: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("holder")
    val holder: String = "",
    @SerializedName("document")
    val document: String = "",
    @SerializedName("legal_account")
    val legalAccount: Boolean = false,
    @SerializedName("operation")
    val operation: String = "",
    @SerializedName("created_at")
    val created: String = "",
    @SerializedName("updated_at")
    val updated: String = "",
    @SerializedName("deleted_at")
    val deleted: String? = null,
    val synced: Boolean = true,
    val bank: Bank? = null,
) {
    fun bankName(): String = if (bank != null) {
        "${bank.name} (${bank.code})"
    } else {
        ""
    }
}
