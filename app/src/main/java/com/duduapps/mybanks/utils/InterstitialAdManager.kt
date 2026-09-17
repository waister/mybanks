package com.duduapps.mybanks.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(
    private val preferencesRepository: PreferencesRepository,
) {
    private var interstitialAd: InterstitialAd? = null

    fun loadAd(context: Context) {
        if (preferencesRepository.havePlan()) return

        val actualAdUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            preferencesRepository.adMobInterstitialId
        }
        if (actualAdUnitId.isEmpty()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            actualAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (BuildConfig.DEBUG) {
                        Log.e(
                            "InterstitialAdManager",
                            "Failed to load interstitial ad: ${error.message} (code: ${error.code})",
                        )
                    }
                    interstitialAd = null
                }
            },
        )
    }

    fun showAd(activity: Activity, onDismissed: () -> Unit = {}) {
        if (preferencesRepository.havePlan() || interstitialAd == null) {
            onDismissed()
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                if (BuildConfig.DEBUG) {
                    Log.e(
                        "InterstitialAdManager",
                        "Failed to show interstitial ad: ${error.message} (code: ${error.code})",
                    )
                }
                interstitialAd = null
                loadAd(activity)
                onDismissed()
            }
        }

        interstitialAd?.show(activity)
    }
}
