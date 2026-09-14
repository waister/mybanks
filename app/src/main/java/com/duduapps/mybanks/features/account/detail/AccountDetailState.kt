package com.duduapps.mybanks.features.account.detail

import com.duduapps.mybanks.models.Account

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val isAccountNotFound: Boolean = false,
)

sealed interface AccountDetailEvent {
    data class ShowToast(val message: String) : AccountDetailEvent
    data class CopyToClipboard(val text: String, val label: String) : AccountDetailEvent
    data class ShareAccount(val text: String) : AccountDetailEvent
    data object NavigateBack : AccountDetailEvent
}
