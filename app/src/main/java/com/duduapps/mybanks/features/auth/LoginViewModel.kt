package com.duduapps.mybanks.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code, codeError = null) }
    }

    fun onPositiveAction() {
        if (_uiState.value.isCodeStep) {
            confirmCode()
        } else {
            sendCode()
        }
    }

    private fun sendCode() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty() || !email.contains("@")) {
            _uiState.update { it.copy(emailError = "Digite um e-mail válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.sendEmailCode(email).fold(
                onSuccess = { response ->
                    val identifier = response.identifier.orEmpty()
                    val decodedVerifier = authRepository.decodeVerifier(response.verifier.orEmpty())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCodeStep = true,
                            identifier = identifier,
                            expectedCode = decodedVerifier,
                        )
                    }
                    _events.emit(LoginEvent.ShowSuccessDialog(response.message ?: "Código enviado com sucesso!"))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.ShowErrorDialog(error.message ?: "Erro ao enviar código"))
                },
            )
        }
    }

    private fun confirmCode() {
        val inputCode = _uiState.value.code.filter { it.isDigit() }
        val expected = _uiState.value.expectedCode

        if (inputCode != expected) {
            _uiState.update { it.copy(codeError = "Código incorreto. Verifique seu e-mail.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.confirmCode(_uiState.value.identifier).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.ShowToast("E-mail verificado com sucesso!"))
                    _events.emit(LoginEvent.LoginSuccess)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.ShowErrorDialog(error.message ?: "Erro ao confirmar código"))
                },
            )
        }
    }
}
