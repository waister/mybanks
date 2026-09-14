package com.duduapps.mybanks.features.splash

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bankRepository: BankRepository = mockk(relaxed = true)
    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { preferencesRepository.isLogged } returns true
        coEvery { bankRepository.fetchAndSaveBanks() } returns Result.success(emptyList())
        coEvery { accountRepository.fetchRemoteAccounts() } returns Result.success(emptyList())
    }

    @Test
    fun `given initial state, when initializeApp called, then emits NavigateToMain event`() = runTest {
        val viewModel = SplashViewModel(
            bankRepository = bankRepository,
            accountRepository = accountRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.events.test {
            viewModel.initializeApp()
            val event = awaitItem()
            assertEquals(SplashEvent.NavigateToMain, event)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { preferencesRepository.ensureDeviceId() }
        coVerify { bankRepository.fetchAndSaveBanks() }
        coVerify { accountRepository.fetchRemoteAccounts() }
    }
}
