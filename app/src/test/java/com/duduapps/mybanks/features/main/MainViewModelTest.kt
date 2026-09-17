package com.duduapps.mybanks.features.main

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.AppConfigRepository
import com.duduapps.mybanks.data.repository.AuthRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private val bankRepository: BankRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    private val testAccounts = listOf(
        Account(
            id = 1L,
            label = "Nubank Pessoal",
            bank = Bank(id = 260, name = "Nubank", code = "260"),
            agency = "0001",
            account = "12345-6",
            holder = "Lucas",
        ),
        Account(
            id = 2L,
            label = "Banco do Brasil PJ",
            bank = Bank(id = 1, name = "Banco do Brasil", code = "001"),
            agency = "4321",
            account = "65432-1",
            holder = "Empresa X",
            legalAccount = true,
        ),
    )

    @Before
    fun setUp() {
        every { preferencesRepository.isLogged } returns true
        every { preferencesRepository.fcmToken } returns ""
        every { accountRepository.getAccountsFlow() } returns flowOf(testAccounts)
        coEvery { bankRepository.fetchAndSaveBanks() } returns Result.success(emptyList())
        coEvery { accountRepository.syncUnsentAccounts() } returns Result.success(Unit)
        coEvery { accountRepository.fetchRemoteAccounts() } returns Result.success(emptyList())
    }

    @Test
    fun `given accounts from repository, when observed, then uiState updates with accounts`() = runTest {
        val viewModel = MainViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            authRepository = authRepository,
            appConfigRepository = appConfigRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.accounts.size)
            assertEquals(2, state.filteredAccounts.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given search query entered, when onSearchQueryChanged called, then filters accounts list`() = runTest {
        val viewModel = MainViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            authRepository = authRepository,
            appConfigRepository = appConfigRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.onSearchQueryChanged("Nubank")

        assertEquals(1, viewModel.uiState.value.filteredAccounts.size)
        assertEquals("Nubank Pessoal", viewModel.uiState.value.filteredAccounts[0].label)
    }

    @Test
    fun `when onCopyAllClicked called, then emits CopyToClipboard and ShowToast events`() = runTest {
        val viewModel = MainViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            authRepository = authRepository,
            appConfigRepository = appConfigRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.events.test {
            viewModel.onCopyAllClicked()
            val event1 = awaitItem()
            assertTrue(event1 is MainEvent.CopyToClipboard)
            val event2 = awaitItem()
            assertTrue(event2 is MainEvent.ShowToast)
            val event3 = awaitItem()
            assertTrue(event3 is MainEvent.ShowInterstitialAd)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when onLogout called, then calls authRepository logout and clears state`() = runTest {
        val viewModel = MainViewModel(
            accountRepository = accountRepository,
            bankRepository = bankRepository,
            authRepository = authRepository,
            appConfigRepository = appConfigRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.onLogout()

        coVerify { authRepository.logout() }
        assertFalse(viewModel.uiState.value.isLogged)
    }
}
