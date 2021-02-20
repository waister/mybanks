package com.duduapps.mybanks.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.util.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_remove_ads.*
import kotlinx.android.synthetic.main.inc_progress_dark.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.okButton

class RemoveAdsActivity : AppCompatActivity() {

    private var adMobRemoveAds: String = ""
    private var planVideoDuration: Long = 0
    private lateinit var rewardedAd: RewardedAd
    private var isRewardedAlertShown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_ads)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0f

        adMobRemoveAds = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5224354917"
        else
            Hawk.get(PREF_ADMOB_REMOVE_ADS, "")

        if (adMobRemoveAds.isEmpty()) {

            longToast(R.string.wait_settings)
            finish()

        } else {

            initViews()
            checkPlan()
            createAndLoadRewardedAd()
        }
    }

    private fun initViews() {
        rl_progress?.visibility = View.VISIBLE

        bt_watch.setOnClickListener {
            if (rewardedAd.isLoaded) {
                rl_progress?.visibility = View.VISIBLE

                val adCallback = object : RewardedAdCallback() {
                    override fun onRewardedAdOpened() {
                        appLog("RemoveAdsActivity :: onRewardedAdOpened()")

                        rl_progress?.visibility = View.GONE
                    }

                    override fun onRewardedAdClosed() {
                        appLog("RemoveAdsActivity :: onRewardedAdClosed()")

                        alertRestartApp()
                    }

                    override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                        appLog("RemoveAdsActivity :: onUserEarnedReward() reward item amount: ${reward.amount}")
                        appLog("RemoveAdsActivity :: onUserEarnedReward() reward item type: ${reward.type}")

                        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())
                    }

                    override fun onRewardedAdFailedToShow(adError: AdError) {
                        appLog("RemoveAdsActivity :: onRewardedAdFailedToShow()")

                        rl_progress?.visibility = View.GONE

                        alert(R.string.error_load_video, R.string.ops) { okButton {} }.show()
                    }
                }
                rewardedAd.show(this, adCallback)
            } else {
                appLog("RemoveAdsActivity :: The rewarded ad wasn't loaded yet.")
            }
        }
    }

    private fun createAndLoadRewardedAd() {
        planVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)

        rewardedAd = RewardedAd(this, adMobRemoveAds)

        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                rl_progress?.visibility = View.GONE

                appLog("RemoveAdsActivity :: onRewardedAdLoaded()")
            }

            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                rl_progress?.visibility = View.GONE

                alert(R.string.error_load_video, R.string.ops) {
                    okButton {
                        finish()
                    }
                }.show()

                appLog("RemoveAdsActivity :: onRewardedAdFailedToLoad()")
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
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
