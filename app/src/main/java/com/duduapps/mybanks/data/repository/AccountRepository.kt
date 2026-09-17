package com.duduapps.mybanks.data.repository

import com.duduapps.mybanks.data.local.dao.AccountDao
import com.duduapps.mybanks.data.local.dao.BankDao
import com.duduapps.mybanks.data.local.entities.toEntity
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.network.ApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface AccountRepository {
    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun saveAccount(account: Account): Result<Account>
    suspend fun deleteAccount(id: Long): Result<Unit>
    suspend fun syncUnsentAccounts(): Result<Unit>
    suspend fun fetchRemoteAccounts(): Result<List<Account>>
    suspend fun clearAccounts()
}

class AccountRepositoryImpl(
    private val apiService: ApiService,
    private val accountDao: AccountDao,
    private val bankDao: BankDao,
    private val preferencesRepository: PreferencesRepository,
) : AccountRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun getAccountsFlow(): Flow<List<Account>> = combine(
        accountDao.getAllAccountsFlow(),
        bankDao.getAllBanksFlow(),
    ) { accounts, banks ->
        val bankMap = banks.associate { it.id to it.toDomain() }
        accounts.map { accountEntity ->
            accountEntity.toDomain(bankMap[accountEntity.bankId])
        }
    }

    override suspend fun getAccountById(id: Long): Account? {
        val entity = accountDao.getAccountById(id) ?: return null
        val bank = bankDao.getBankById(entity.bankId)?.toDomain()
        return entity.toDomain(bank)
    }

    override suspend fun saveAccount(account: Account): Result<Account> = runCatching {
        val now = dateFormat.format(Date())
        val isNew = account.id == 0L
        val accountId = if (isNew) System.currentTimeMillis() else account.id
        val createdTime = if (isNew) now else account.created.ifEmpty { now }

        val entityToSave = account.copy(
            id = accountId,
            created = createdTime,
            updated = now,
            deleted = null,
            synced = false,
        ).toEntity()

        accountDao.insertAccount(entityToSave)

        syncUnsentAccounts()

        getAccountById(accountId) ?: entityToSave.toDomain()
    }

    override suspend fun deleteAccount(id: Long): Result<Unit> = runCatching {
        val now = dateFormat.format(Date())
        accountDao.markAsDeleted(id, now)

        syncUnsentAccounts()
    }

    override suspend fun syncUnsentAccounts(): Result<Unit> = runCatching {
        val unsynced = accountDao.getUnsyncedAccounts()
        for (entity in unsynced) {
            val response = apiService.registerAccount(
                id = entity.id,
                pixCode = entity.pixCode,
                bankId = entity.bankId,
                label = entity.label,
                type = entity.type,
                agency = entity.agency,
                account = entity.account,
                operation = entity.operation,
                holder = entity.holder,
                document = entity.document,
                legalAccount = entity.legalAccount,
                deletedAt = entity.deleted,
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                if (entity.deleted != null) {
                    accountDao.deleteAccountById(entity.id)
                } else {
                    accountDao.updateAccount(entity.copy(synced = true))
                }

                val remoteAccounts = body.accounts
                if (remoteAccounts.isNotEmpty()) {
                    accountDao.insertAccounts(
                        remoteAccounts.map { it.toEntity() },
                    )
                }
            }
        }
    }

    override suspend fun fetchRemoteAccounts(): Result<List<Account>> = runCatching {
        if (!preferencesRepository.isLogged) {
            return@runCatching emptyList()
        }

        val response = apiService.getAccounts()
        if (response.isSuccessful && response.body()?.success == true) {
            val remoteAccounts = response.body()!!.accounts
            if (remoteAccounts.isNotEmpty()) {
                accountDao.insertAccounts(
                    remoteAccounts.map { it.toEntity() },
                )
            }
            remoteAccounts.map { it.toDomain() }
        } else {
            val errorMsg = response.body()?.message ?: "Falha ao sincronizar contas"
            throw Exception(errorMsg)
        }
    }

    override suspend fun clearAccounts() {
        accountDao.clearAccounts()
    }
}
