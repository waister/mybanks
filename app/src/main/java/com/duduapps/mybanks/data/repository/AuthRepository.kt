package com.duduapps.mybanks.data.repository

import android.os.Build
import com.duduapps.mybanks.models.EmailCodeResponse
import com.duduapps.mybanks.network.ApiService

interface AuthRepository {
    suspend fun sendEmailCode(email: String): Result<EmailCodeResponse>
    suspend fun confirmCode(identifier: String): Result<Unit>
    suspend fun logout()
    fun decodeVerifier(base64Verifier: String): String
}

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val preferencesRepository: PreferencesRepository,
    private val accountRepository: AccountRepository,
) : AuthRepository {

    override suspend fun sendEmailCode(email: String): Result<EmailCodeResponse> = runCatching {
        val response = apiService.sendEmailCode(email)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()!!
        } else {
            val errorMsg = response.body()?.message ?: "Falha ao enviar código para o e-mail"
            throw Exception(errorMsg)
        }
    }

    override suspend fun confirmCode(identifier: String): Result<Unit> = runCatching {
        preferencesRepository.deviceIdOld = identifier
        preferencesRepository.isLogged = true
        accountRepository.fetchRemoteAccounts()
    }

    override suspend fun logout() {
        preferencesRepository.logout()
        accountRepository.clearAccounts()
    }

    override fun decodeVerifier(base64Verifier: String): String {
        return try {
            val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.util.Base64.getDecoder().decode(base64Verifier)
            } else {
                android.util.Base64.decode(base64Verifier, android.util.Base64.DEFAULT)
            }
            String(data, Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }
}
