package com.duduapps.mybanks.features.account.form

import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private val bankRepository: BankRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    private val testBank = Bank(id = 260, name = "Nubank", code = "260")

    @Before
    fun setUp() {
        coEvery { bankRepository.getBanks() } returns listOf(testBank)
        every { preferencesRepository.lastHolder } returns "João Silva"
        every { preferencesRepository.lastDocument } returns "12345678900"
        every { preferencesRepository.lastLegalAccount } returns false
    }

    @Test
    fun `given new account, when initialize called, then loads last holder and document`() = runTest {
        val viewModel = AccountFormViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.initialize(0L)

        val state = viewModel.uiState.value
        assertEquals("João Silva", state.holder)
        assertEquals("12345678900", state.document)
    }

    @Test
    fun `given existing account, when initialize called, then populates edit fields`() = runTest {
        val account = Account(
            id = 10L,
            label = "Minha Conta Editada",
            bank = testBank,
            bankId = 260,
            agency = "0001",
            account = "98765-4",
            type = "Conta Corrente",
            holder = "Maria",
            document = "11122233344",
        )
        coEvery { accountRepository.getAccountById(10L) } returns account

        val viewModel = AccountFormViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.initialize(10L)

        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals("Minha Conta Editada", state.label)
        assertEquals("98765-4", state.account)
    }

    @Test
    fun `given empty fields, when onSubmit called, then shows validation errors`() = runTest {
        val viewModel = AccountFormViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            preferencesRepository = preferencesRepository,
        )
        viewModel.initialize(0L)
        viewModel.onHolderChanged("")
        viewModel.onDocumentChanged("")

        viewModel.onSubmit()

        val state = viewModel.uiState.value
        assertNotNull(state.labelError)
        assertNotNull(state.bankError)
        assertNotNull(state.agencyError)
        assertNotNull(state.accountError)
        assertNotNull(state.holderError)
        assertNotNull(state.documentError)
    }

    @Test
    fun `given valid fields, when onSubmit called, then saves account and triggers dialog`() = runTest {
        coEvery { accountRepository.saveAccount(any()) } returns Result.success(mockk())

        val viewModel = AccountFormViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            preferencesRepository = preferencesRepository,
        )
        viewModel.initialize(0L)
        viewModel.onLabelChanged("Conta Principal")
        viewModel.onBankSelected(testBank)
        viewModel.onAgencyChanged("1234")
        viewModel.onAccountChanged("12345-6")
        viewModel.onHolderChanged("João")
        viewModel.onDocumentChanged("12345678900")

        viewModel.onSubmit()

        coVerify { accountRepository.saveAccount(any()) }
        assertTrue(viewModel.uiState.value.isSuccessDialogVisible)
    }
}
