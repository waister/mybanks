package com.duduapps.mybanks.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager(
    private val application: Application,
    private val preferencesRepository: PreferencesRepository,
) : Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun loadAd() {
        if (isAdAvailable || preferencesRepository.havePlan()) return

        val adUnitId = preferencesRepository.adMobOpenAppId
        if (adUnitId.isEmpty()) return

        val actualAdUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else adUnitId
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            actualAdUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                }
            },
        )
    }

    fun showAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (isShowingAd || preferencesRepository.havePlan()) {
            onComplete()
            return
        }

        if (!isAdAvailable) {
            loadAd()
            onComplete()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAd()
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd()
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) currentActivity = null
    }
}
