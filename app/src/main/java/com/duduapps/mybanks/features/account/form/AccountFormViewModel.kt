package com.duduapps.mybanks.features.account.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountFormViewModel(
    private val accountRepository: AccountRepository,
    private val bankRepository: BankRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountFormUiState())
    val uiState: StateFlow<AccountFormUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AccountFormEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AccountFormEvent> = _events.asSharedFlow()

    fun initialize(accountId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val banks = bankRepository.getBanks()
            _uiState.update { it.copy(banks = banks) }

            if (accountId > 0) {
                val account = accountRepository.getAccountById(accountId)
                if (account != null) {
                    val bank = banks.find { it.id == account.bankId } ?: account.bank
                    _uiState.update {
                        it.copy(
                            accountId = account.id,
                            isEditMode = true,
                            label = account.label,
                            selectedBank = bank,
                            pixCode = account.pixCode,
                            agency = account.agency,
                            account = account.account,
                            operation = account.operation,
                            type = account.type,
                            holder = account.holder,
                            isLegalAccount = account.legalAccount,
                            document = account.document,
                            isLoading = false,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AccountFormEvent.ShowToast("Conta não encontrada"))
                    _events.emit(AccountFormEvent.NavigateBack)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isEditMode = false,
                        holder = preferencesRepository.lastHolder,
                        document = preferencesRepository.lastDocument,
                        isLegalAccount = preferencesRepository.lastLegalAccount,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onLabelChanged(value: String) {
        _uiState.update { it.copy(label = value, labelError = null) }
    }

    fun onBankSelected(bank: Bank) {
        _uiState.update { it.copy(selectedBank = bank, bankError = null) }
    }

    fun onPixCodeChanged(value: String) {
        _uiState.update { it.copy(pixCode = value) }
    }

    fun onAgencyChanged(value: String) {
        _uiState.update { it.copy(agency = value, agencyError = null) }
    }

    fun onAccountChanged(value: String) {
        val digits = value.filter { it.isDigit() }
        val formatted = if (digits.length >= 2) {
            "${digits.dropLast(1)}-${digits.takeLast(1)}"
        } else {
            digits
        }
        _uiState.update { it.copy(account = formatted, accountError = null) }
    }

    fun onOperationChanged(value: String) {
        _uiState.update { it.copy(operation = value) }
    }

    fun onTypeChanged(value: String) {
        _uiState.update { it.copy(type = value) }
    }

    fun onHolderChanged(value: String) {
        _uiState.update { it.copy(holder = value, holderError = null) }
    }

    fun onLegalAccountChanged(isLegal: Boolean) {
        _uiState.update { it.copy(isLegalAccount = isLegal, documentError = null) }
    }

    fun onDocumentChanged(value: String) {
        _uiState.update { it.copy(document = value, documentError = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        var hasError = false

        var labelError: String? = null
        var bankError: String? = null
        var agencyError: String? = null
        var accountError: String? = null
        var holderError: String? = null
        var documentError: String? = null

        if (state.label.isBlank()) {
            labelError = "Digite a identificação da conta"
            hasError = true
        }

        if (state.selectedBank == null) {
            bankError = "Selecione o banco"
            hasError = true
        }

        if (state.agency.isBlank()) {
            agencyError = "Digite a agência"
            hasError = true
        } else if (state.agency.length < 4) {
            agencyError = "Agência inválida (mínimo 4 dígitos)"
            hasError = true
        }

        if (state.account.isBlank()) {
            accountError = "Digite o número da conta"
            hasError = true
        } else if (state.account.length < 3) {
            accountError = "Conta inválida (mínimo 3 dígitos)"
            hasError = true
        }

        if (state.holder.isBlank()) {
            holderError = "Digite o nome do titular"
            hasError = true
        }

        if (state.document.isBlank()) {
            documentError = if (state.isLegalAccount) "Digite o CNPJ" else "Digite o CPF"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    labelError = labelError,
                    bankError = bankError,
                    agencyError = agencyError,
                    accountError = accountError,
                    holderError = holderError,
                    documentError = documentError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            preferencesRepository.lastHolder = state.holder
            preferencesRepository.lastDocument = state.document
            preferencesRepository.lastLegalAccount = state.isLegalAccount

            val accountToSave = Account(
                id = state.accountId,
                pixCode = state.pixCode,
                bankId = state.selectedBank!!.id,
                label = state.label,
                agency = state.agency,
                account = state.account,
                type = state.type,
                holder = state.holder,
                document = state.document,
                legalAccount = state.isLegalAccount,
                operation = state.operation,
                bank = state.selectedBank,
            )

            accountRepository.saveAccount(accountToSave)

            _uiState.update { it.copy(isLoading = false) }

            if (state.isEditMode) {
                _events.emit(AccountFormEvent.ShowToast("Conta alterada com sucesso!"))
                _events.emit(AccountFormEvent.NavigateBack)
            } else {
                _uiState.update { it.copy(isSuccessDialogVisible = true) }
            }
        }
    }

    fun onResetFormForNewAccount() {
        _uiState.update {
            it.copy(
                accountId = 0L,
                isEditMode = false,
                label = "",
                selectedBank = null,
                pixCode = "",
                agency = "",
                account = "",
                operation = "",
                type = "Conta Corrente",
                isSuccessDialogVisible = false,
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSuccessDialogVisible = false) }
    }
}
