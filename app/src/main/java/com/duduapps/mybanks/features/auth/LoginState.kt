package com.duduapps.mybanks.features.auth

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val code: String = "",
    val codeError: String? = null,
    val isCodeStep: Boolean = false,
    val identifier: String = "",
    val expectedCode: String = "",
    val isLoading: Boolean = false,
)

sealed interface LoginEvent {
    data class ShowToast(val message: String) : LoginEvent
    data class ShowSuccessDialog(val message: String) : LoginEvent
    data class ShowErrorDialog(val message: String) : LoginEvent
    data object LoginSuccess : LoginEvent
    data object NavigateBack : LoginEvent
}
