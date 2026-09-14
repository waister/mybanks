package com.duduapps.mybanks.features.auth

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.AuthRepository
import com.duduapps.mybanks.models.EmailCodeResponse
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

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository = authRepository)
    }

    @Test
    fun `given invalid email, when sendCode called, then sets email error`() {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPositiveAction()

        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `given valid email, when sendCode called successfully, then transitions to code step`() = runTest {
        val response = EmailCodeResponse(
            success = true,
            message = "Código enviado",
            identifier = "ident123",
            verifier = "MTIzNA==",
        )
        coEvery { authRepository.sendEmailCode("user@example.com") } returns Result.success(response)
        coEvery { authRepository.decodeVerifier("MTIzNA==") } returns "1234"

        viewModel.onEmailChanged("user@example.com")
        viewModel.onPositiveAction()

        val state = viewModel.uiState.value
        assertTrue(state.isCodeStep)
        assertEquals("ident123", state.identifier)
        assertEquals("1234", state.expectedCode)
    }

    @Test
    fun `given correct code, when confirmCode called, then confirms in repository and emits LoginSuccess`() = runTest {
        val response = EmailCodeResponse(
            success = true,
            message = "Código enviado",
            identifier = "ident123",
            verifier = "MTIzNA==",
        )
        coEvery { authRepository.sendEmailCode("user@example.com") } returns Result.success(response)
        coEvery { authRepository.decodeVerifier("MTIzNA==") } returns "1234"
        coEvery { authRepository.confirmCode("ident123") } returns Result.success(Unit)

        viewModel.onEmailChanged("user@example.com")
        viewModel.onPositiveAction()

        viewModel.events.test {
            viewModel.onCodeChanged("1234")
            viewModel.onPositiveAction()

            val event1 = awaitItem()
            assertTrue(event1 is LoginEvent.ShowToast)
            val event2 = awaitItem()
            assertEquals(LoginEvent.LoginSuccess, event2)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { authRepository.confirmCode("ident123") }
    }
}
