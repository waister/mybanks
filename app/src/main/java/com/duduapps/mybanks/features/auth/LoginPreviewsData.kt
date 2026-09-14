package com.duduapps.mybanks.features.auth

object LoginPreviewsData {
    val emailStepState = LoginUiState(
        isCodeStep = false,
        email = "usuario@exemplo.com",
    )

    val emailErrorState = LoginUiState(
        isCodeStep = false,
        email = "email_invalido",
        emailError = "E-mail inválido",
    )

    val codeStepState = LoginUiState(
        isCodeStep = true,
        email = "usuario@exemplo.com",
        code = "1234",
    )

    val codeErrorState = LoginUiState(
        isCodeStep = true,
        email = "usuario@exemplo.com",
        code = "0000",
        codeError = "Código inválido",
    )

    val loadingState = LoginUiState(
        isCodeStep = false,
        email = "usuario@exemplo.com",
        isLoading = true,
    )
}
