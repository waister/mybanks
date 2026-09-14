package com.duduapps.mybanks.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.AppConfigRepository
import com.duduapps.mybanks.data.repository.AuthRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.models.Account
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val accountRepository: AccountRepository,
    private val bankRepository: BankRepository,
    private val authRepository: AuthRepository,
    private val appConfigRepository: AppConfigRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()

    init {
        observeAccounts()
        refreshData()
        checkVersion()
    }

    private fun observeAccounts() {
        accountRepository.getAccountsFlow()
            .onEach { accounts ->
                _uiState.update { state ->
                    val filtered = filterAccounts(accounts, state.searchQuery)
                    val showLoginAlert = !preferencesRepository.isLogged &&
                        preferencesRepository.showAlertLogin &&
                        accounts.isNotEmpty()

                    state.copy(
                        accounts = accounts,
                        filteredAccounts = filtered,
                        isLogged = preferencesRepository.isLogged,
                        showAlertLogin = showLoginAlert,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isLogged = preferencesRepository.isLogged) }
            bankRepository.fetchAndSaveBanks()
            accountRepository.syncUnsentAccounts()
            if (preferencesRepository.isLogged) {
                accountRepository.fetchRemoteAccounts()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredAccounts = filterAccounts(state.accounts, query),
            )
        }
    }

    fun toggleSearch(active: Boolean) {
        _uiState.update { state ->
            val query = if (!active) "" else state.searchQuery
            state.copy(
                isSearchActive = active,
                searchQuery = query,
                filteredAccounts = filterAccounts(state.accounts, query),
            )
        }
    }

    private fun filterAccounts(accounts: List<Account>, query: String): List<Account> {
        return if (query.isEmpty()) {
            accounts.sortedBy { it.label.lowercase() }
        } else {
            accounts.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.holder.contains(query, ignoreCase = true) ||
                    (it.bank?.name?.contains(query, ignoreCase = true) == true) ||
                    it.pixCode.contains(query, ignoreCase = true)
            }.sortedBy { it.label.lowercase() }
        }
    }

    fun onCopyAllClicked() {
        val text = formatAccountsText(_uiState.value.filteredAccounts)
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                _events.emit(MainEvent.CopyToClipboard(text))
                _events.emit(MainEvent.ShowToast("Contas copiadas!"))
                _events.emit(MainEvent.ShowInterstitialAd)
            }
        }
    }

    fun onShareAllClicked() {
        val text = formatAccountsText(_uiState.value.filteredAccounts)
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                _events.emit(MainEvent.ShareAccounts(text))
                _events.emit(MainEvent.ShowInterstitialAd)
            }
        }
    }

    fun onDismissLoginAlert(neverShowAgain: Boolean = false) {
        if (neverShowAgain) {
            preferencesRepository.showAlertLogin = false
        }
        _uiState.update { it.copy(showAlertLogin = false) }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLogged = false, accounts = emptyList(), filteredAccounts = emptyList()) }
            _events.emit(MainEvent.ShowToast("Você saiu da sua conta"))
        }
    }

    private fun formatAccountsText(accounts: List<Account>): String {
        return accounts.joinToString("\n--\n") { account ->
            buildString {
                if (account.pixCode.isNotEmpty()) append("PIX: ${account.pixCode}\n")
                append("Banco: ${account.bankName()}\n")
                append("Agência: ${account.agency}\n")
                append("Conta: ${account.account}\n")
                if (account.operation.isNotEmpty()) append("Operação: ${account.operation}\n")
                append("Tipo: ${account.type}\n")
                append("Titular: ${account.holder}\n")
                val docLabel = if (account.legalAccount) "CNPJ" else "CPF"
                append("$docLabel: ${account.document}")
            }
        }
    }

    private fun checkVersion() {
        val token = preferencesRepository.fcmToken
        if (token.isEmpty()) return

        viewModelScope.launch {
            appConfigRepository.identify(token).onSuccess { response ->
                val currentVersion = BuildConfig.VERSION_CODE
                val storeUrl = preferencesRepository.storeLink.ifEmpty {
                    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                }
                if (currentVersion < response.versionMin) {
                    _uiState.update {
                        it.copy(
                            isUpdateDialogVisible = true,
                            isMandatoryUpdate = true,
                            updateStoreUrl = storeUrl,
                        )
                    }
                } else if (currentVersion < response.versionLast) {
                    _uiState.update {
                        it.copy(
                            isUpdateDialogVisible = true,
                            isMandatoryUpdate = false,
                            updateStoreUrl = storeUrl,
                        )
                    }
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(isUpdateDialogVisible = false) }
    }
}
