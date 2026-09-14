package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.data.local.dao.BankDao
import com.duduapps.mybanks.data.local.entities.toEntity
import com.duduapps.mybanks.models.Bank
import com.duduapps.mybanks.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BankRepository {
    fun getBanksFlow(): Flow<List<Bank>>
    suspend fun getBanks(): List<Bank>
    suspend fun getBankById(id: Int): Bank?
    suspend fun fetchAndSaveBanks(): Result<List<Bank>>
}

class BankRepositoryImpl(
    private val apiService: ApiService,
    private val bankDao: BankDao,
    private val preferencesRepository: PreferencesRepository,
) : BankRepository {

    override fun getBanksFlow(): Flow<List<Bank>> =
        bankDao.getAllBanksFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getBanks(): List<Bank> =
        bankDao.getAllBanks().map { it.toDomain() }

    override suspend fun getBankById(id: Int): Bank? =
        bankDao.getBankById(id)?.toDomain()

    override suspend fun fetchAndSaveBanks(): Result<List<Bank>> = runCatching {
        val response = apiService.getBanks()
        if (response.isSuccessful && response.body()?.success == true) {
            val body = response.body()!!
            body.storeLink?.let { preferencesRepository.storeLink = it }
            body.appName?.let { preferencesRepository.appName = it }
            body.admobId?.let { preferencesRepository.adMobId = it }
            body.admobAdMainId?.let { preferencesRepository.adMobAdMainId = it }
            body.admobInterstitialId?.let { preferencesRepository.adMobInterstitialId = it }
            body.admobOpenAppId?.let { preferencesRepository.adMobOpenAppId = it }
            body.admobRemoveAds?.let { preferencesRepository.adMobRemoveAds = it }
            if (body.planVideoDuration > 0) {
                preferencesRepository.planVideoDuration = body.planVideoDuration
            }

            val banks = body.banks
            if (banks.isNotEmpty()) {
                bankDao.insertBanks(banks.map { it.toEntity() })
            }
            banks
        } else {
            val errorMsg = response.body()?.message ?: "Falha ao carregar bancos"
            throw Exception(errorMsg)
        }
    }
}
