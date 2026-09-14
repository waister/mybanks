package com.duduapps.mybanks.features.account.detail

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountRepository: AccountRepository = mockk(relaxed = true)

    private val testAccount = Account(
        id = 1L,
        label = "Minha Conta",
        bank = Bank(id = 1, name = "Banco do Brasil", code = "001"),
        agency = "1234",
        account = "5678-9",
        holder = "João",
        document = "12345678900",
    )

    @Before
    fun setUp() {
        coEvery { accountRepository.getAccountById(1L) } returns testAccount
        coEvery { accountRepository.deleteAccount(1L) } returns Result.success(Unit)
    }

    @Test
    fun `given valid id, when loadAccount called, then populates account details in state`() = runTest {
        val viewModel = AccountDetailViewModel(accountRepository = accountRepository)

        viewModel.loadAccount(1L)

        val state = viewModel.uiState.value
        assertNotNull(state.account)
        assertEquals("Minha Conta", state.account?.label)
        assertEquals("Banco do Brasil", state.account?.bank?.name)
    }

    @Test
    fun `when onCopyField called, then emits CopyToClipboard and ShowToast events`() = runTest {
        val viewModel = AccountDetailViewModel(accountRepository = accountRepository)

        viewModel.events.test {
            viewModel.onCopyField("1234", "Agência")
            val event1 = awaitItem()
            assertTrue(event1 is AccountDetailEvent.CopyToClipboard)
            val event2 = awaitItem()
            assertTrue(event2 is AccountDetailEvent.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when onDeleteAccount called, then deletes account in repository and navigates back`() = runTest {
        val viewModel = AccountDetailViewModel(accountRepository = accountRepository)
        viewModel.loadAccount(1L)

        viewModel.events.test {
            viewModel.onDeleteAccount()
            val event1 = awaitItem()
            assertTrue(event1 is AccountDetailEvent.ShowToast)
            val event2 = awaitItem()
            assertEquals(AccountDetailEvent.NavigateBack, event2)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { accountRepository.deleteAccount(1L) }
    }
}
