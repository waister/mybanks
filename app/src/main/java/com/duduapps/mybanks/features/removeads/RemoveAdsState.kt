package com.duduapps.mybanks.features.removeads

data class RemoveAdsUiState(
    val hasPlan: Boolean = false,
    val remainingDays: Long = 0L,
    val isAdLoaded: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface RemoveAdsEvent {
    data class ShowToast(val message: String) : RemoveAdsEvent
    data object ShowRewardedAd : RemoveAdsEvent
    data object AdRewardSuccess : RemoveAdsEvent
}
