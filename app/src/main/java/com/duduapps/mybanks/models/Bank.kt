package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class Bank(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("code")
    val code: String = "",
) {
    fun getDisplayName(): String = "$code - $name"
}
