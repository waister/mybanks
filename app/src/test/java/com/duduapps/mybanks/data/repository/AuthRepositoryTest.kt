package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.BaseRobolectricTest
import com.duduapps.mybanks.models.EmailCodeResponse
import com.duduapps.mybanks.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest : BaseRobolectricTest() {

    private val apiService: ApiService = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val accountRepository: AccountRepository = mockk(relaxed = true)

    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(
            apiService = apiService,
            preferencesRepository = preferencesRepository,
            accountRepository = accountRepository,
        )
    }

    @Test
    fun `given email, when sendEmailCode called successfully, then returns response`() = runTest {
        val response = EmailCodeResponse(
            success = true,
            identifier = "device123",
            verifier = "MTIzNA==",
        )
        coEvery { apiService.sendEmailCode("test@example.com") } returns Response.success(response)

        val result = repository.sendEmailCode("test@example.com")

        assertTrue(result.isSuccess)
        assertEquals("device123", result.getOrNull()?.identifier)
    }

    @Test
    fun `given base64 verifier, when decodeVerifier called, then returns decoded string`() {
        val decoded = repository.decodeVerifier("MTIzNA==")
        assertEquals("1234", decoded)
    }

    @Test
    fun `given user confirmation, when confirmCode called, then updates preferences and fetches remote accounts`() = runTest {
        val result = repository.confirmCode("device123")

        assertTrue(result.isSuccess)
        coVerify { preferencesRepository.deviceIdOld = "device123" }
        coVerify { preferencesRepository.isLogged = true }
        coVerify { accountRepository.fetchRemoteAccounts() }
    }

    @Test
    fun `when logout called, then clears preferences and accounts`() = runTest {
        repository.logout()

        coVerify { preferencesRepository.logout() }
        coVerify { accountRepository.clearAccounts() }
    }
}
