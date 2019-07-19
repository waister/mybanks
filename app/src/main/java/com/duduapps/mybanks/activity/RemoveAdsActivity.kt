package com.duduapps.mybanks.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.util.*
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_remove_ads.*
import kotlinx.android.synthetic.main.inc_progress_dark.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.okButton

class RemoveAdsActivity : AppCompatActivity(), RewardedVideoAdListener {

    companion object {
        const val TAG = "RemoveAdsActivity"
    }

    private var admobRemoveAds: String = ""
    private var planVideoDuration: Long = 0
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_ads)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0f

        if (BuildConfig.DEBUG)
            admobRemoveAds = "ca-app-pub-3940256099942544/5224354917"
        else
            admobRemoveAds = Hawk.get(PREF_ADMOB_REMOVE_ADS, "")

        if (admobRemoveAds.isEmpty()) {

            longToast(R.string.wait_settings)
            finish()

        } else {

            planVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)

            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
            mRewardedVideoAd.rewardedVideoAdListener = this

            bt_watch.setOnClickListener {
                rl_progress?.visibility = View.VISIBLE

                val adRequest = getAdRequest()

                if (adRequest != null)
                    mRewardedVideoAd.loadAd(admobRemoveAds, adRequest)
            }

            checkPlan()
        }
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

    override fun onRewardedVideoAdClosed() {
        Log.w(TAG, "onRewardedVideoAdClosed()")
    }

    override fun onRewardedVideoAdLeftApplication() {
        Log.w(TAG, "onRewardedVideoAdLeftApplication()")
    }

    override fun onRewardedVideoAdLoaded() {
        Log.w(TAG, "onRewardedVideoAdLoaded()")

        mRewardedVideoAd.show()

        rl_progress?.visibility = View.GONE
    }

    override fun onRewardedVideoAdOpened() {
        Log.w(TAG, "onRewardedVideoAdOpened()")
    }

    override fun onRewardedVideoCompleted() {
        Log.w(TAG, "onRewardedVideoCompleted()")
    }

    override fun onRewarded(reward: RewardItem?) {
        Log.w(TAG, "onRewarded() reward item amount: ${reward?.amount}")
        Log.w(TAG, "onRewarded() reward item type: ${reward?.type}")

        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())

        val days = checkPlan()

        if (days > 0) {
            val title = getString(R.string.tank_you)
            val body = getString(R.string.watch_to_by_tanks, days)

            alert (body, title) { okButton { finish() } }.show()
        }
    }

    override fun onRewardedVideoStarted() {
        Log.w(TAG, "onRewardedVideoStarted()")
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Log.w(TAG, "onRewardedVideoAdFailedToLoad() error code: $errorCode")

        rl_progress?.visibility = View.GONE

        longToast(R.string.error_load_video)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
