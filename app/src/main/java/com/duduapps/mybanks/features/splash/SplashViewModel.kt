package com.duduapps.mybanks.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val bankRepository: BankRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SplashEvent>()
    val events: SharedFlow<SplashEvent> = _events.asSharedFlow()

    init {
        initializeApp()
    }

    fun initializeApp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Inicializando...") }

            preferencesRepository.ensureDeviceId()

            _uiState.update { it.copy(statusMessage = "Carregando bancos...") }
            bankRepository.fetchAndSaveBanks()

            if (preferencesRepository.isLogged) {
                _uiState.update { it.copy(statusMessage = "Carregando contas...") }
                accountRepository.fetchRemoteAccounts()
            }

            _uiState.update { it.copy(isLoading = false) }
            _events.emit(SplashEvent.NavigateToMain)
        }
    }
}
