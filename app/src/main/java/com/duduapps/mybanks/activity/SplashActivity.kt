package com.duduapps.mybanks.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.application.CustomApplication
import com.duduapps.mybanks.model.Bank
import com.duduapps.mybanks.util.*
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast

class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
    }

    private val realm = Realm.getDefaultInstance()
    private var secondsWait: Long = 0
    private var runnableFreights: Runnable = Runnable { apiGetBanks() }
    private var handlerFreights: Handler = Handler()
    private var runnableAccounts: Runnable = Runnable { apiGetAccounts() }
    private var handlerAccounts: Handler = Handler()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.w(TAG, "Token FCM: " + Hawk.get(PREF_FCM_TOKEN, ""))

        if (!isLogged()) {
            Hawk.put(PREF_IDENTIFIER, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            CustomApplication().updateFuelParams()
        }

        var updateOnSplash = Hawk.get(PREF_UPDATE_ON_SPLASH, true)

        if (Hawk.get(PREF_PLAN_VIDEO_DURATION, 0L) == 0L)
            updateOnSplash = true

        if (Hawk.get(PREF_INTERSTITIAL_MIN_INTERVAL, 0L) == 0L)
            updateOnSplash = true

        val banks = realm.where(Bank::class.java).count()

        if (updateOnSplash || banks == 0L) {

            apiGetBanks()

        } else {

            initApp()

        }
    }

    private fun initApp() {
        startActivity(intentFor<MainActivity>())
        finish()
    }

    private fun apiGetBanks() {
        if (secondsWait > 0) {
            secondsWait--

            pb_loading.visibility = View.GONE
            tv_label.text = getString(R.string.trying_again_in, secondsWait)

            delayCheckBanks()
        } else {

            pb_loading.visibility = View.VISIBLE
            tv_label.setText(R.string.loading_banks)

            API_ROUTE_BANKS.httpGet().responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result
                var success = false
                var message = getString(R.string.states_api_error)

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    success = apiObj.getBooleanVal(API_SUCCESS)
                    message = apiObj.getStringVal(API_MESSAGE)

                    if (success) {
                        Hawk.put(PREF_UPDATE_ON_SPLASH, false)

                        success = realm.saveBanks(result)

                        if (success) {

                            apiGetAccounts()

                        } else {

                            success = false

                        }
                    }
                }

                if (message.isNotEmpty()) {
                    longToast(message)
                }

                if (!success) {
                    secondsWait = 30
                    delayCheckBanks()
                }
            }
        }
    }

    private fun apiGetAccounts() {
        if (!isLogged() || realm.unsentAccountsCount() > 0) {
            initApp()
            return
        }

        if (secondsWait > 0) {
            secondsWait--

            pb_loading.visibility = View.GONE
            tv_label.text = getString(R.string.trying_again_in, secondsWait)

            delayCheckAccounts()
        } else {

            pb_loading.visibility = View.VISIBLE
            tv_label.setText(R.string.loading_accounts)

            API_ROUTE_ACCOUNTS.httpGet().responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result
                var success = false
                var message = getString(R.string.states_api_error)

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    message = apiObj.getStringVal(API_MESSAGE)

                    realm.saveAccounts(result)

                    initApp()

                    success = true
                }

                if (message.isNotEmpty()) {
                    longToast(message)
                }

                if (!success) {
                    secondsWait = 30
                    delayCheckAccounts()
                }
            }
        }
    }

    private fun delayCheckBanks() {
        handlerFreights.removeCallbacks(runnableFreights)
        handlerFreights.postDelayed(runnableFreights, 1000)
    }

    private fun delayCheckAccounts() {
        handlerAccounts.removeCallbacks(runnableAccounts)
        handlerAccounts.postDelayed(runnableAccounts, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerFreights.removeCallbacks(runnableFreights)
        handlerAccounts.removeCallbacks(runnableAccounts)
        realm?.close()
    }

}
