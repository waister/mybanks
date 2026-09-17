package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.data.local.dao.BankDao
import com.duduapps.mybanks.data.local.entities.BankEntity
import com.duduapps.mybanks.models.Bank
import com.duduapps.mybanks.models.BanksResponse
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

class BankRepositoryTest {

    private val apiService: ApiService = mockk(relaxed = true)
    private val bankDao: BankDao = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    private lateinit var repository: BankRepository

    @Before
    fun setUp() {
        repository = BankRepositoryImpl(
            apiService = apiService,
            bankDao = bankDao,
            preferencesRepository = preferencesRepository,
        )
    }

    @Test
    fun `given banks in local database, when getBanks called, then returns domain bank list`() = runTest {
        val entities = listOf(
            BankEntity(id = 1, name = "Banco do Brasil", code = "001"),
            BankEntity(id = 2, name = "Nubank", code = "260"),
        )
        coEvery { bankDao.getAllBanks() } returns entities

        val result = repository.getBanks()

        assertEquals(2, result.size)
        assertEquals("Banco do Brasil", result[0].name)
        assertEquals("001", result[0].code)
    }

    @Test
    fun `given successful api response, when fetchAndSaveBanks called, then inserts into database`() = runTest {
        val banks = listOf(Bank(id = 1, name = "Banco do Brasil", code = "001"))
        val response = BanksResponse(
            success = true,
            banks = banks,
            appName = "My Banks",
        )
        coEvery { apiService.getBanks() } returns Response.success(response)

        val result = repository.fetchAndSaveBanks()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify { bankDao.insertBanks(any()) }
    }
}
