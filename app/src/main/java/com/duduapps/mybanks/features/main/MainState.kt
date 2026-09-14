package com.duduapps.mybanks.features.main

import com.duduapps.mybanks.models.Account

data class MainUiState(
    val accounts: List<Account> = emptyList(),
    val filteredAccounts: List<Account> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val isLogged: Boolean = false,
    val showAlertLogin: Boolean = false,
    val isUpdateDialogVisible: Boolean = false,
    val isMandatoryUpdate: Boolean = false,
    val updateStoreUrl: String = "",
)

sealed interface MainEvent {
    data class ShowToast(val message: String) : MainEvent
    data class ShareAccounts(val text: String) : MainEvent
    data class CopyToClipboard(val text: String) : MainEvent
    data class OpenUrl(val url: String) : MainEvent
    data object ShowInterstitialAd : MainEvent
}
