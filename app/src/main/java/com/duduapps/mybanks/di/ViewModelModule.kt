package com.duduapps.mybanks.di

import com.duduapps.mybanks.features.account.detail.AccountDetailViewModel
import com.duduapps.mybanks.features.account.form.AccountFormViewModel
import com.duduapps.mybanks.features.auth.LoginViewModel
import com.duduapps.mybanks.features.feedback.FeedbackViewModel
import com.duduapps.mybanks.features.main.MainViewModel
import com.duduapps.mybanks.features.removeads.RemoveAdsViewModel
import com.duduapps.mybanks.features.splash.SplashViewModel
import com.duduapps.mybanks.utils.AppOpenAdManager
import com.duduapps.mybanks.utils.InterstitialAdManager
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::AccountDetailViewModel)
    viewModelOf(::AccountFormViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RemoveAdsViewModel)
    viewModelOf(::FeedbackViewModel)

    single { InterstitialAdManager(preferencesRepository = get()) }
    single { AppOpenAdManager(application = get(), preferencesRepository = get()) }
}
