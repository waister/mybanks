package com.duduapps.mybanks.features.splash

data class SplashUiState(
    val isLoading: Boolean = true,
    val statusMessage: String = "Carregando...",
    val retrySeconds: Int = 0,
)

sealed interface SplashEvent {
    data object NavigateToMain : SplashEvent
    data class ShowToast(val message: String) : SplashEvent
}
