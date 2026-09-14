package com.duduapps.mybanks.data.repository

import app.cash.turbine.test
import com.duduapps.mybanks.data.local.dao.AccountDao
import com.duduapps.mybanks.data.local.dao.BankDao
import com.duduapps.mybanks.data.local.entities.AccountEntity
import com.duduapps.mybanks.data.local.entities.BankEntity
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountRepositoryTest {

    private val apiService: ApiService = mockk(relaxed = true)
    private val accountDao: AccountDao = mockk(relaxed = true)
    private val bankDao: BankDao = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    private lateinit var repository: AccountRepository

    @Before
    fun setUp() {
        repository = AccountRepositoryImpl(
            apiService = apiService,
            accountDao = accountDao,
            bankDao = bankDao,
            preferencesRepository = preferencesRepository,
        )
    }

    @Test
    fun `given accounts and banks in database, when getAccountsFlow collected, then emits merged accounts`() = runTest {
        val accountEntities = listOf(
            AccountEntity(
                id = 1L,
                pixCode = "test@pix.com",
                bankId = 1,
                label = "Minha Conta",
                agency = "1234",
                account = "56789-0",
                type = "Conta Corrente",
                holder = "João",
                document = "12345678900",
                legalAccount = false,
                operation = "",
                created = "2026-01-01 10:00:00",
                updated = "2026-01-01 10:00:00",
                deleted = null,
                synced = true,
            ),
        )
        val bankEntities = listOf(
            BankEntity(id = 1, name = "Banco do Brasil", code = "001"),
        )

        coEvery { accountDao.getAllAccountsFlow() } returns flowOf(accountEntities)
        coEvery { bankDao.getAllBanksFlow() } returns flowOf(bankEntities)

        repository.getAccountsFlow().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Minha Conta", list[0].label)
            assertEquals("Banco do Brasil", list[0].bank?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given account to save, when saveAccount called, then inserts into DAO and triggers sync`() = runTest {
        val account = Account(
            id = 0L,
            label = "Nova Conta",
            bankId = 1,
            agency = "1234",
            account = "5678-9",
            type = "Conta Corrente",
            holder = "Maria",
            document = "98765432100",
        )
        coEvery { accountDao.getUnsyncedAccounts() } returns emptyList()

        val result = repository.saveAccount(account)

        assertTrue(result.isSuccess)
        coVerify { accountDao.insertAccount(any()) }
    }

    @Test
    fun `given account id, when deleteAccount called, then marks as deleted in DAO`() = runTest {
        coEvery { accountDao.getUnsyncedAccounts() } returns emptyList()

        val result = repository.deleteAccount(1L)

        assertTrue(result.isSuccess)
        coVerify { accountDao.markAsDeleted(1L, any()) }
    }
}
