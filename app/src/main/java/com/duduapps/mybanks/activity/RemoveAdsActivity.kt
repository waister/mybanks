package com.duduapps.mybanks.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.util.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_remove_ads.*
import kotlinx.android.synthetic.main.inc_progress_dark.*
import org.jetbrains.anko.longToast

class RemoveAdsActivity : AppCompatActivity(), OnUserEarnedRewardListener {

    private var adMobRemoveAds: String = ""
    private var isRewardedAlertShown: Boolean = false
    private var rewardedAd: RewardedAd? = null

    companion object {
        private const val TAG = "RemoveAdsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_ads)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0f

        adMobRemoveAds = Hawk.get(PREF_ADMOB_REMOVE_ADS, "")
        appLog(TAG, "Ad unit id: $adMobRemoveAds")

        if (adMobRemoveAds.isEmpty()) {

            longToast(R.string.wait_settings)
            finish()

        } else {

            initViews()
            checkPlan()
        }
    }

    private fun initViews() {
        rl_progress_dark?.visibility = View.VISIBLE

        MobileAds.initialize(this) {
            appLog(TAG, "Mobile ads initialized")

            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration =
                RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            val request = AdRequest.Builder().build()

            RewardedAd.load(
                this,
                adMobRemoveAds,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        appLog(TAG, "Filed to load ad: ${adError.message}")

                        alertErrorLoad()

                        rewardedAd = null
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        appLog(TAG, "Ad was loaded")

                        rl_progress_dark?.visibility = View.GONE

                        rewardedAd = ad

                        rewardedAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    appLog(TAG, "Ad was dismissed")

                                    alertRestartApp()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    appLog(TAG, "Ad failed to show: ${adError.message}")

                                    alertErrorLoad()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    appLog(TAG, "Ad showed fullscreen content")

                                    rewardedAd = null
                                }
                            }
                    }
                })
        }

        bt_watch.setOnClickListener {
            val canShow = rewardedAd != null
            appLog(TAG, "Subscribe button clicked - can show: $canShow")

            if (canShow) {
                rewardedAd?.show(this, this)
            } else {
                appLog(TAG, "The rewarded ad wasn't ready yet")
            }
        }
    }

    override fun onUserEarnedReward(rewardedAd: RewardItem) {
        val amount = rewardedAd.amount
        val type = rewardedAd.type
        appLog(TAG, "User earned the reward amount: $amount")
        appLog(TAG, "User earned the reward type: $type")

        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())
    }

    private fun alertErrorLoad() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.ops)
            .setMessage(R.string.error_load_video)
            .setPositiveButton(R.string.ok) { _, _ ->
                restartApp()
            }
            .create()
            .show()
    }

    private fun alertRestartApp() {
        if (!isRewardedAlertShown && havePlan()) {
            isRewardedAlertShown = true

            AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.plan_success_title)
                .setMessage(R.string.plan_success_body)
                .setPositiveButton(R.string.restart_app) { _, _ ->
                    restartApp()
                }
                .create()
                .show()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        finish()
    }

    private fun checkPlan(): Long {
        val planVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)

        if (havePlan()) {

            val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + planVideoDuration
            val days = ((expiration - System.currentTimeMillis()) / ONE_DAY) + 1
            tv_body.text = getString(R.string.watch_to_by_body_paid, days)

            bt_watch.setText(R.string.watch_to_by_button_again)

            return days

        } else {

            val days = planVideoDuration / ONE_DAY
            tv_body.text = getString(R.string.watch_to_by_body, days)

        }

        return 0L
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
