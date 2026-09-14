package com.duduapps.mybanks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.duduapps.mybanks.data.local.dao.AccountDao
import com.duduapps.mybanks.data.local.dao.BankDao
import com.duduapps.mybanks.data.local.entities.AccountEntity
import com.duduapps.mybanks.data.local.entities.BankEntity

@Database(
    entities = [
        BankEntity::class,
        AccountEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun accountDao(): AccountDao
}
