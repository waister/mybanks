package com.duduapps.mybanks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duduapps.mybanks.data.local.entities.BankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {

    @Query("SELECT * FROM banks ORDER BY code ASC")
    fun getAllBanksFlow(): Flow<List<BankEntity>>

    @Query("SELECT * FROM banks ORDER BY code ASC")
    suspend fun getAllBanks(): List<BankEntity>

    @Query("SELECT * FROM banks WHERE id = :id LIMIT 1")
    suspend fun getBankById(id: Int): BankEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanks(banks: List<BankEntity>)

    @Query("DELETE FROM banks")
    suspend fun clearBanks()
}
