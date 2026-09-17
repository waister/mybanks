package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.models.IdentifyResponse
import com.duduapps.mybanks.network.ApiService

interface AppConfigRepository {
    suspend fun identify(token: String): Result<IdentifyResponse>
}

class AppConfigRepositoryImpl(
    private val apiService: ApiService,
) : AppConfigRepository {

    override suspend fun identify(token: String): Result<IdentifyResponse> = runCatching {
        val response = apiService.identify(token)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()!!
        } else {
            val errorMsg = response.body()?.message ?: "Falha ao consultar configurações"
            throw Exception(errorMsg)
        }
    }
}
