package com.duduapps.mybanks.di

import com.duduapps.mybanks.data.repository.AccountRepository
import com.duduapps.mybanks.data.repository.AccountRepositoryImpl
import com.duduapps.mybanks.data.repository.AppConfigRepository
import com.duduapps.mybanks.data.repository.AppConfigRepositoryImpl
import com.duduapps.mybanks.data.repository.AuthRepository
import com.duduapps.mybanks.data.repository.AuthRepositoryImpl
import com.duduapps.mybanks.data.repository.BankRepository
import com.duduapps.mybanks.data.repository.BankRepositoryImpl
import com.duduapps.mybanks.data.repository.FeedbackRepository
import com.duduapps.mybanks.data.repository.FeedbackRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<BankRepository> {
        BankRepositoryImpl(
            apiService = get(),
            bankDao = get(),
            preferencesRepository = get(),
        )
    }

    single<AccountRepository> {
        AccountRepositoryImpl(
            apiService = get(),
            accountDao = get(),
            bankDao = get(),
            preferencesRepository = get(),
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            apiService = get(),
            preferencesRepository = get(),
            accountRepository = get(),
        )
    }

    single<FeedbackRepository> {
        FeedbackRepositoryImpl(
            apiService = get(),
            preferencesRepository = get(),
        )
    }

    single<AppConfigRepository> {
        AppConfigRepositoryImpl(
            apiService = get(),
        )
    }
}
