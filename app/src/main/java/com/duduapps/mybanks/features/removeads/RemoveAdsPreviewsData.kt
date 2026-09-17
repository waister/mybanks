package com.duduapps.mybanks.features.removeads

object RemoveAdsPreviewsData {
    val freePlanAdLoadedState = RemoveAdsUiState(
        hasPlan = false,
        remainingDays = 0L,
        isAdLoaded = true,
        isLoading = false,
    )

    val freePlanAdLoadingState = RemoveAdsUiState(
        hasPlan = false,
        remainingDays = 0L,
        isAdLoaded = false,
        isLoading = false,
    )

    val activePlanState = RemoveAdsUiState(
        hasPlan = true,
        remainingDays = 5L,
        isAdLoaded = true,
        isLoading = false,
    )
}
