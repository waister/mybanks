package com.duduapps.mybanks.di

import androidx.room.Room
import com.duduapps.mybanks.data.local.AppDatabase
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.data.repository.PreferencesRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single<PreferencesRepository> {
        PreferencesRepositoryImpl(androidContext())
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mybanks_database",
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().bankDao() }
    single { get<AppDatabase>().accountDao() }
}
