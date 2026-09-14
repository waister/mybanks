package com.duduapps.mybanks.features.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.FeedbackRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val feedbackRepository: FeedbackRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>()
    val events: SharedFlow<FeedbackEvent> = _events.asSharedFlow()

    init {
        _uiState.update {
            it.copy(
                name = preferencesRepository.name,
                email = preferencesRepository.email,
                comments = preferencesRepository.comments,
            )
        }
    }

    fun onNameChanged(value: String) {
        preferencesRepository.name = value
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onEmailChanged(value: String) {
        preferencesRepository.email = value
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onCommentsChanged(value: String) {
        preferencesRepository.comments = value
        _uiState.update { it.copy(comments = value, commentsError = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        var hasError = false

        var nameError: String? = null
        var emailError: String? = null
        var commentsError: String? = null

        if (state.name.isBlank()) {
            nameError = "Digite seu nome"
            hasError = true
        }

        if (state.email.isBlank()) {
            emailError = "Digite seu e-mail"
            hasError = true
        }

        if (state.comments.isBlank()) {
            commentsError = "Digite sua mensagem"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    commentsError = commentsError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            feedbackRepository.sendFeedback(state.name, state.email, state.comments).fold(
                onSuccess = { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comments = "",
                        )
                    }
                    _events.emit(FeedbackEvent.ShowSuccessDialog(message))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(FeedbackEvent.ShowErrorDialog(error.message ?: "Erro ao enviar feedback"))
                },
            )
        }
    }
}
