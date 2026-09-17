package com.duduapps.mybanks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duduapps.mybanks.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE deleted IS NULL ORDER BY label ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE deleted IS NULL ORDER BY label ASC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id AND deleted IS NULL LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountByIdIncludingDeleted(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE synced = 0")
    suspend fun getUnsyncedAccounts(): List<AccountEntity>

    @Query("SELECT COUNT(*) FROM accounts WHERE synced = 0")
    suspend fun getUnsyncedAccountsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET deleted = :deletedTimestamp, synced = 0 WHERE id = :id")
    suspend fun markAsDeleted(id: Long, deletedTimestamp: String)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()
}
