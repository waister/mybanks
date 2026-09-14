package com.duduapps.mybanks.features.feedback

object FeedbackPreviewsData {
    val emptyState = FeedbackUiState(
        name = "",
        email = "",
        comments = "",
    )

    val filledState = FeedbackUiState(
        name = "Carlos Drummond",
        email = "carlos@exemplo.com",
        comments = "Parabéns pelo aplicativo! Adorei a nova interface moderna com Jetpack Compose.",
    )

    val errorState = FeedbackUiState(
        name = "",
        nameError = "O nome é obrigatório",
        email = "email_invalido",
        emailError = "E-mail inválido",
        comments = "",
        commentsError = "A mensagem é obrigatória",
    )

    val loadingState = FeedbackUiState(
        name = "Carlos Drummond",
        email = "carlos@exemplo.com",
        comments = "Enviando sugestão...",
        isLoading = true,
    )
}
