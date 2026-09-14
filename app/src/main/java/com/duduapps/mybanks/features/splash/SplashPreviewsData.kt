package com.duduapps.mybanks.features.splash

object SplashPreviewsData {
    val loadingState = SplashUiState(
        isLoading = true,
        statusMessage = "Sincronizando contas com o servidor...",
        retrySeconds = 0,
    )

    val idleState = SplashUiState(
        isLoading = false,
        statusMessage = "",
        retrySeconds = 0,
    )
}
