package com.duduapps.mybanks

import android.app.Application
import com.duduapps.mybanks.di.appModule
import com.duduapps.mybanks.di.localModule
import com.duduapps.mybanks.di.networkModule
import com.duduapps.mybanks.di.repositoryModule
import com.duduapps.mybanks.di.viewModelModule
import com.duduapps.mybanks.utils.AppOpenAdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

        if (BuildConfig.DEBUG) {
            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }

        MobileAds.initialize(this) {
            appOpenAdManager.loadAd()
        }

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }
}
