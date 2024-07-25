package com.duduapps.mybanks.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.databinding.ActivityRemoveAdsBinding
import com.duduapps.mybanks.util.ONE_DAY
import com.duduapps.mybanks.util.PREF_ADMOB_REMOVE_ADS
import com.duduapps.mybanks.util.PREF_PLAN_VIDEO_DURATION
import com.duduapps.mybanks.util.PREF_PLAN_VIDEO_MILLIS
import com.duduapps.mybanks.util.appLog
import com.duduapps.mybanks.util.havePlan
import com.duduapps.mybanks.util.hide
import com.duduapps.mybanks.util.show
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import org.jetbrains.anko.longToast

class RemoveAdsActivity : AppCompatActivity(), OnUserEarnedRewardListener {

    private lateinit var binding: ActivityRemoveAdsBinding

    private var adMobRemoveAds: String = ""
    private var isRewardedAlertShown: Boolean = false
    private var rewardedAd: RewardedAd? = null

    companion object {
        private const val TAG = "RemoveAdsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemoveAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.progress.rlProgressDark.show()

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

                        binding.progress.rlProgressDark.hide()

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

        binding.btWatch.setOnClickListener {
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
        val intent = Intent(this, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        finish()
    }

    private fun checkPlan(): Long = with(binding) {
        val planVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)

        if (havePlan()) {

            val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + planVideoDuration
            val days = ((expiration - System.currentTimeMillis()) / ONE_DAY) + 1
            tvBody.text = getString(R.string.watch_to_by_body_paid, days)

            btWatch.setText(R.string.watch_to_by_button_again)

            return days

        } else {

            val days = planVideoDuration / ONE_DAY
            tvBody.text = getString(R.string.watch_to_by_body, days)

        }

        return 0L
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
