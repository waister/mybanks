package com.duduapps.mybanks.models

import com.google.gson.annotations.SerializedName

data class BanksResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("store_link")
    val storeLink: String? = null,
    @SerializedName("app_name")
    val appName: String? = null,
    @SerializedName("admob_id")
    val admobId: String? = null,
    @SerializedName("admob_ad_main_id")
    val admobAdMainId: String? = null,
    @SerializedName("admob_interstitial_id")
    val admobInterstitialId: String? = null,
    @SerializedName("admob_open_app_id")
    val admobOpenAppId: String? = null,
    @SerializedName("admob_remove_ads")
    val admobRemoveAds: String? = null,
    @SerializedName("plan_video_duration")
    val planVideoDuration: Long = 0L,
    @SerializedName("banks")
    val banks: List<Bank> = emptyList(),
)
