package com.duduapps.mybanks

import android.app.Application
import com.duduapps.mybanks.di.appModule
import com.duduapps.mybanks.di.localModule
import com.duduapps.mybanks.di.networkModule
import com.duduapps.mybanks.di.repositoryModule
import com.duduapps.mybanks.di.viewModelModule
import com.duduapps.mybanks.utils.AppOpenAdManager
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CustomApplication : Application() {

    private val appOpenAdManager: AppOpenAdManager by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CustomApplication)
            modules(
                appModule,
                localModule,
                networkModule,
                repositoryModule,
                viewModelModule,
            )
        }

        MobileAds.initialize(this) {}
        appOpenAdManager.loadAd()
    }
}
