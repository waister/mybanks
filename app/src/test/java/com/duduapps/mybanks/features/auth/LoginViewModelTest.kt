package com.duduapps.mybanks.features.auth

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.AuthRepository
import com.duduapps.mybanks.models.EmailCodeResponse
import com.duduapps.mybanks.utils.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeAuthRepository : AuthRepository {
        var sendEmailCodeResult: Result<EmailCodeResponse> = Result.failure(Exception("Not set"))
        var confirmCodeResult: Result<Unit> = Result.failure(Exception("Not set"))
        var confirmCodeCalledWith: String? = null

        override suspend fun sendEmailCode(email: String): Result<EmailCodeResponse> = sendEmailCodeResult

        override suspend fun confirmCode(identifier: String): Result<Unit> {
            confirmCodeCalledWith = identifier
            return confirmCodeResult
        }

        override suspend fun logout() {}

        override fun decodeVerifier(base64Verifier: String): String = "1234"
    }

    @Test
    fun `given invalid email, when sendCode called, then sets email error`() {
        val fakeRepo = FakeAuthRepository()
        val viewModel = LoginViewModel(authRepository = fakeRepo)
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPositiveAction()

        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `given valid email, when sendCode called successfully, then transitions to code step`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            sendEmailCodeResult = Result.success(
                EmailCodeResponse(
                    success = true,
                    message = "Código enviado",
                    identifier = "ident123",
                    verifier = "MTIzNA==",
                ),
            )
        }

        val viewModel = LoginViewModel(authRepository = fakeRepo)
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPositiveAction()

        val state = viewModel.uiState.value
        assertTrue(state.isCodeStep)
        assertEquals("ident123", state.identifier)
        assertEquals("1234", state.expectedCode)
    }

    @Test
    fun `given correct code, when confirmCode called, then confirms in repository and emits LoginSuccess`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            sendEmailCodeResult = Result.success(
                EmailCodeResponse(
                    success = true,
                    message = "Código enviado",
                    identifier = "ident123",
                    verifier = "MTIzNA==",
                ),
            )
            confirmCodeResult = Result.success(Unit)
        }

        val viewModel = LoginViewModel(authRepository = fakeRepo)

        viewModel.events.test {
            viewModel.onEmailChanged("user@example.com")
            viewModel.onPositiveAction()

            val event1 = awaitItem()
            assertTrue(event1 is LoginEvent.ShowSuccessDialog)

            viewModel.onCodeChanged("1234")
            viewModel.onPositiveAction()

            val event2 = awaitItem()
            assertTrue(event2 is LoginEvent.ShowToast)
            val event3 = awaitItem()
            assertEquals(LoginEvent.LoginSuccess, event3)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("ident123", fakeRepo.confirmCodeCalledWith)
    }
}
