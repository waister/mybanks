package com.duduapps.mybanks.features.feedback

data class FeedbackUiState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val comments: String = "",
    val commentsError: String? = null,
    val isLoading: Boolean = false,
)

sealed interface FeedbackEvent {
    data class ShowToast(val message: String) : FeedbackEvent
    data class ShowSuccessDialog(val message: String) : FeedbackEvent
    data class ShowErrorDialog(val message: String) : FeedbackEvent
    data object NavigateBack : FeedbackEvent
}
