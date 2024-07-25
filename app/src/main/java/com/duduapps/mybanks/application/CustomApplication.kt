package com.duduapps.mybanks.application

import android.app.Application
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.util.API_ANDROID
import com.duduapps.mybanks.util.API_DEBUG
import com.duduapps.mybanks.util.API_IDENTIFIER
import com.duduapps.mybanks.util.API_IDENTIFIER_OLD
import com.duduapps.mybanks.util.API_PLATFORM
import com.duduapps.mybanks.util.API_ROOT
import com.duduapps.mybanks.util.API_V
import com.duduapps.mybanks.util.API_VERSION
import com.duduapps.mybanks.util.AppOpenManager
import com.duduapps.mybanks.util.PREF_DEVICE_ID
import com.duduapps.mybanks.util.PREF_DEVICE_ID_OLD
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.ads.MobileAds
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import io.realm.RealmConfiguration

class CustomApplication : Application() {

    var isCheckUpdatesNeeded: Boolean = true

    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()

        MobileAds.initialize(this) {}

        AppOpenManager(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .schemaVersion(REALM_VERSION)
                .deleteRealmIfMigrationNeeded()
                .build()
        )

        FuelManager.instance.basePath = API_ROOT

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
            API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
            API_IDENTIFIER_OLD to Hawk.get(PREF_DEVICE_ID_OLD, ""),
            API_VERSION to BuildConfig.VERSION_CODE,
            API_PLATFORM to API_ANDROID,
            API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
            API_V to 8
        )
    }

    companion object {
        const val REALM_VERSION: Long = 1
    }
}