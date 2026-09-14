package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.network.ApiService

interface FeedbackRepository {
    suspend fun sendFeedback(name: String, email: String, comments: String): Result<String>
}

class FeedbackRepositoryImpl(
    private val apiService: ApiService,
    private val preferencesRepository: PreferencesRepository,
) : FeedbackRepository {

    override suspend fun sendFeedback(name: String, email: String, comments: String): Result<String> = runCatching {
        val response = apiService.sendFeedback(name, email, comments)
        if (response.isSuccessful && response.body()?.success == true) {
            preferencesRepository.clearFeedbackCache()
            response.body()?.message ?: "Feedback enviado com sucesso!"
        } else {
            val errorMsg = response.body()?.message ?: "Falha ao enviar mensagem"
            throw Exception(errorMsg)
        }
    }
}
