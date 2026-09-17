package com.duduapps.mybanks.features.account.form

import com.duduapps.mybanks.models.Bank

data class AccountFormUiState(
    val accountId: Long = 0L,
    val isEditMode: Boolean = false,
    val label: String = "",
    val labelError: String? = null,
    val selectedBank: Bank? = null,
    val bankError: String? = null,
    val banks: List<Bank> = emptyList(),
    val pixCode: String = "",
    val agency: String = "",
    val agencyError: String? = null,
    val account: String = "",
    val accountError: String? = null,
    val operation: String = "",
    val type: String = "Conta Corrente",
    val holder: String = "",
    val holderError: String? = null,
    val isLegalAccount: Boolean = false,
    val document: String = "",
    val documentError: String? = null,
    val isLoading: Boolean = false,
    val isSuccessDialogVisible: Boolean = false,
)

sealed interface AccountFormEvent {
    data class ShowToast(val message: String) : AccountFormEvent
    data object AccountSaved : AccountFormEvent
    data object NavigateBack : AccountFormEvent
}
