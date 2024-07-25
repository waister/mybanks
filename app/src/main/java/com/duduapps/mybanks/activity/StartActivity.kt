package com.duduapps.mybanks.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.application.CustomApplication
import com.duduapps.mybanks.databinding.ActivityStartBinding
import com.duduapps.mybanks.model.Bank
import com.duduapps.mybanks.util.API_MESSAGE
import com.duduapps.mybanks.util.API_ROUTE_ACCOUNTS
import com.duduapps.mybanks.util.API_ROUTE_BANKS
import com.duduapps.mybanks.util.API_SUCCESS
import com.duduapps.mybanks.util.PREF_DEVICE_ID
import com.duduapps.mybanks.util.PREF_DEVICE_ID_OLD
import com.duduapps.mybanks.util.PREF_FCM_TOKEN
import com.duduapps.mybanks.util.PREF_PLAN_VIDEO_DURATION
import com.duduapps.mybanks.util.PREF_UPDATE_ON_SPLASH
import com.duduapps.mybanks.util.appLog
import com.duduapps.mybanks.util.getBooleanVal
import com.duduapps.mybanks.util.getStringVal
import com.duduapps.mybanks.util.getValidJSONObject
import com.duduapps.mybanks.util.hide
import com.duduapps.mybanks.util.isLogged
import com.duduapps.mybanks.util.isNotNumeric
import com.duduapps.mybanks.util.printFuelLog
import com.duduapps.mybanks.util.saveAccounts
import com.duduapps.mybanks.util.saveBanks
import com.duduapps.mybanks.util.show
import com.duduapps.mybanks.util.unsentAccountsCount
import com.github.kittinunf.fuel.httpGet
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import java.util.Calendar
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private val realm = Realm.getDefaultInstance()
    private var secondsWait: Long = 0
    private var runnableBanks: Runnable = Runnable { apiGetBanks() }
    private var handlerBanks: Handler = Handler(Looper.getMainLooper())
    private var runnableAccounts: Runnable = Runnable { apiGetAccounts() }
    private var handlerAccounts: Handler = Handler(Looper.getMainLooper())

    private var appUpdateManager: AppUpdateManager? = null
    private var updateFlowResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            appLog(
                "IN_APP_UPDATE",
                "Launcher result code: ${result.resultCode} |  ${result.resultCode == RESULT_OK}"
            )

            if (result.resultCode == RESULT_OK) {
                initApp()
            } else {
                binding.pbLoading.hide()

                AlertDialog.Builder(this)
                    .setTitle(R.string.error_on_update)
                    .setMessage(R.string.error_on_update_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.try_again) { dialog, _ ->
                        checkAppVersion()

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.update_later) { dialog, _ ->
                        initApp()

                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

    companion object {
        private const val LOG_TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appLog(LOG_TAG, "Token FCM: " + Hawk.get(PREF_FCM_TOKEN, ""))

        createDeviceID()

        var updateOnSplash = Hawk.get(PREF_UPDATE_ON_SPLASH, true)

        if (Hawk.get(PREF_PLAN_VIDEO_DURATION, 0L) == 0L)
            updateOnSplash = true

        val banks = realm.where(Bank::class.java).count()

        if (updateOnSplash || banks == 0L) {

            apiGetBanks()

        } else {

            initApp()

        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                appLog("IN_APP_UPDATE", "On resume error: ${it.message}")

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateFlowResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }

    private fun initApp() {
        if ((application as CustomApplication).isCheckUpdatesNeeded) {
            (application as CustomApplication).isCheckUpdatesNeeded = false

            checkAppVersion()
        } else {
            startActivity(intentFor<MainActivity>())
            finish()
        }
    }

    private fun apiGetBanks() = with(binding) {
        if (secondsWait > 0) {
            secondsWait--

            pbLoading.hide()
            tvLabel.text = getString(R.string.trying_again_in, secondsWait)

            delayCheckBanks()
        } else {

            pbLoading.show()
            tvLabel.setText(R.string.loading_banks)

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

    private fun apiGetAccounts() = with(binding) {
        if (!isLogged() || realm.unsentAccountsCount() > 0) {
            initApp()
            return@with
        }

        if (secondsWait > 0) {
            secondsWait--

            pbLoading.hide()
            tvLabel.text = getString(R.string.trying_again_in, secondsWait)

            delayCheckAccounts()
        } else {

            pbLoading.show()
            tvLabel.setText(R.string.loading_accounts)

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
        handlerBanks.apply {
            removeCallbacks(runnableBanks)
            postDelayed(runnableBanks, 1000)
        }
    }

    private fun delayCheckAccounts() {
        handlerAccounts.apply {
            removeCallbacks(runnableAccounts)
            postDelayed(runnableAccounts, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerBanks.apply {
            removeCallbacks(runnableBanks)
            removeCallbacks(runnableAccounts)
        }
        realm?.close()

        updateFlowResultLauncher.unregister()
    }

    private fun createDeviceID() {
        if (!isLogged()) {
            val currentDeviceID = Hawk.get(PREF_DEVICE_ID, "")
            val isNotNumeric = currentDeviceID.isNotNumeric()

            if (currentDeviceID.isEmpty() || isNotNumeric) {
                if (isNotNumeric) Hawk.put(PREF_DEVICE_ID_OLD, currentDeviceID)

                val milliseconds = Calendar.getInstance().timeInMillis.toString()
                val random = Random.nextInt(10000, 99999)
                var stringID = "$milliseconds$random"

                if (stringID.length > 18) {
                    stringID = stringID.substring(0, 18)
                } else if (stringID.length < 18) {
                    stringID = stringID.padEnd(18, '9')
                }

                Hawk.put(PREF_DEVICE_ID, stringID)
                CustomApplication().updateFuelParams()

                appLog("GENERATE_DEVICE_ID", "New device ID: $stringID")
            } else {
                appLog("GENERATE_DEVICE_ID", "Ignored, current ID: $currentDeviceID")
            }
        }
    }

    private fun checkAppVersion() {
        binding.pbLoading.show()

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                initApp()

                appLog("IN_APP_UPDATE", "Error message: ${it.message}")

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                appLog("IN_APP_UPDATE", "Success - appUpdateInfo: $appUpdateInfo")

                val updateAvailable =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                val appUpdateType = when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                    else -> null
                }

                if (updateAvailable && appUpdateType != null) {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateFlowResultLauncher,
                        AppUpdateOptions.newBuilder(appUpdateType).build()
                    )
                } else
                    initApp()
            }
    }

}
