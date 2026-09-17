package com.duduapps.mybanks.features.account.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.models.Account
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AccountDetailEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AccountDetailEvent> = _events.asSharedFlow()

    fun loadAccount(accountId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                _uiState.update { it.copy(isLoading = false, account = account, isAccountNotFound = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, isAccountNotFound = true) }
                _events.emit(AccountDetailEvent.ShowToast("Conta não encontrada"))
                _events.emit(AccountDetailEvent.NavigateBack)
            }
        }
    }

    fun onCopyField(text: String, label: String) {
        viewModelScope.launch {
            _events.emit(AccountDetailEvent.CopyToClipboard(text, label))
            _events.emit(AccountDetailEvent.ShowToast("Dado copiado!"))
        }
    }

    fun onCopyAll() {
        val account = _uiState.value.account ?: return
        val text = formatAccountText(account)
        viewModelScope.launch {
            _events.emit(AccountDetailEvent.CopyToClipboard(text, account.label))
            _events.emit(AccountDetailEvent.ShowToast("Conta copiada!"))
        }
    }

    fun onShareAll() {
        val account = _uiState.value.account ?: return
        val text = formatAccountText(account)
        viewModelScope.launch {
            _events.emit(AccountDetailEvent.ShareAccount(text))
        }
    }

    fun onDeleteAccount() {
        val account = _uiState.value.account ?: return
        viewModelScope.launch {
            accountRepository.deleteAccount(account.id)
            _events.emit(AccountDetailEvent.ShowToast("Conta excluída"))
            _events.emit(AccountDetailEvent.NavigateBack)
        }
    }

    private fun formatAccountText(account: Account): String = buildString {
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
