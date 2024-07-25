package com.duduapps.mybanks.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.activity.StartActivity
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.model.Bank
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


const val PARAM_ID = "ParamId"
const val PARAM_TYPE = "ParamType"
const val PARAM_ITEM_ID = "ParamItemId"

const val ONE_SECOND: Long = 1000
const val ONE_MINUTE: Long = ONE_SECOND * 60
const val ONE_HOUR: Long = ONE_MINUTE * 60
const val ONE_DAY: Long = ONE_HOUR * 24

fun isLogged(): Boolean {
    return Hawk.get(PREF_LOGGED, false)
}

fun Activity.logout() {
    val context = this

    alert(
        getString(R.string.confirm_logout_account),
        getString(R.string.logout_account)
    ) {
        positiveButton(R.string.confirm) {
            Hawk.delete(PREF_LOGGED)

            val intent = Intent(context, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        }
        negativeButton(R.string.cancel) {}
    }.show()
}

fun Context.storeAppLink(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun havePlan(): Boolean {
    val planVideoMillis = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L)
    if (planVideoMillis != 0L) {
        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)
        val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
        return expiration > System.currentTimeMillis()
    }
    return false
}

fun Context?.loadAdMobBanner(
    adViewContainer: LinearLayout?,
    adUnitId: String,
    adSize: AdSize? = null,
    collapsible: Boolean = false
) {
    val logTag = "LOAD_ADMOB_BANNER"

    if (this == null || adUnitId.isEmpty() || adViewContainer == null || havePlan()) {
        appLog(logTag, "loadAdMobBanner() falied | $this | $adUnitId | ${havePlan()}")
        return
    }

    appLog(logTag, "adUnitId: $adUnitId")

    val adView = AdView(this)
    adViewContainer.addView(adView)

    adView.adUnitId = if (isDebug()) "ca-app-pub-3940256099942544/6300978111" else adUnitId

    adView.setAdSize(adSize ?: getAdSize(adViewContainer))

    val extras = Bundle()
    if (collapsible) {
        extras.putString("collapsible", "bottom")
        extras.putString("collapsible_request_id", UUID.randomUUID().toString())
    }

    val adRequest = AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        .build()

    adView.loadAd(adRequest)

    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            appLog(logTag, "onAdLoaded()")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            appLog(logTag, "onAdFailedToLoad(): ${error.message}")
        }

        override fun onAdOpened() {
            super.onAdOpened()
            appLog(logTag, "onAdOpened()")
        }

        override fun onAdClosed() {
            super.onAdClosed()
            appLog(logTag, "onAdClosed()")
        }
    }

    appLog(logTag, "ENDS")
}

fun Context.getAdSize(adViewContainer: LinearLayout): AdSize {
    var adWidthPixels = adViewContainer.width.toFloat()
    if (adWidthPixels == 0f)
        adWidthPixels = displayWidth().toFloat()

    val density = resources.displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}

fun Context?.displayWidth(): Int {
    return if (this != null) resources.displayMetrics.widthPixels else 0
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(getString(R.string.app_name), text)
    clipboard.setPrimaryClip(clip)
}

fun printFuelLog(request: Request, response: Response, result: Result<String, FuelError>) {
    if (BuildConfig.DEBUG) {
        println("\n--------------- REQUEST_REQUEST_START - ${request.url}\n")
        println(request)
        println("\n--------------- REQUEST_REQUEST_END - ${request.url}\n")
        println("\n--------------- RESPONSE_RESPONSE_START - ${request.url}\n")
        println(response)
        println("\n--------------- RESPONSE_RESPONSE_END - ${request.url}\n")
        println("\n--------------- RESULT_RESULT_START - ${request.url}\n")
        println(result)
        println("\n--------------- RESULT_RESULT_END - ${request.url}\n")
    }
}

fun appLog(tag: String, msg: String) {
    if (BuildConfig.DEBUG)
        Log.i("MAGGAPPS_LOG", "➡➡➡ $tag: $msg")
}

fun String?.stringToInt(): Int {
    if (this != null && this != "null") {
        val number = this.replace("\\D".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.isValidUrl(): Boolean {
    return !this.isNullOrEmpty() && URLUtil.isValidUrl(this)
}

fun String?.getApiImage(): String {
    if (this != null) {
        if (!contains("http") && contains("/uploads/")) {
            val path = APP_HOST.removeSuffix("/") + this

            if (path.isValidUrl()) {
                return path
            }
        }

        return this
    }

    return ""
}

fun Context?.getThumbUrl(
    image: String?,
    width: Int = 220,
    height: Int = 0,
    quality: Int = 85
): String {
    if (this != null && image != null && !image.contains("http") && image.contains("/uploads/")) {
        return APP_HOST + "thumb?src=$image&w=$width&h=$height&q=$quality"
    }

    return image.getApiImage()
}

fun Bitmap?.getCircleCroppedBitmap(): Bitmap? {
    var output: Bitmap? = null
    val bitmap = this

    if (bitmap != null) {
        try {
            output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            if (bitmap.width < bitmap.height) {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.width / 2).toFloat(), paint
                )
            } else {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.height / 2).toFloat(), paint
                )
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return output
}

fun Realm?.saveBanks(result: Result<String, FuelError>): Boolean {
    val (data, error) = result

    if (this != null && !this.isClosed && error == null) {
        val apiObj = data.getValidJSONObject()

        if (apiObj.getBooleanVal(API_SUCCESS)) {
            Hawk.put(PREF_SHARE_LINK, apiObj.getStringVal(API_SHARE_LINK))
            Hawk.put(PREF_APP_NAME, apiObj.getStringVal(API_APP_NAME))
            Hawk.put(PREF_ADMOB_ID, apiObj.getStringVal(API_ADMOB_ID))
            Hawk.put(PREF_ADMOB_AD_MAIN_ID, apiObj.getStringVal(API_ADMOB_AD_MAIN_ID))
            Hawk.put(PREF_ADMOB_INTERSTITIAL_ID, apiObj.getStringVal(API_ADMOB_INTERSTITIAL_ID))
            Hawk.put(PREF_ADMOB_OPEN_APP_ID, apiObj.getStringVal(API_ADMOB_OPEN_APP_ID))
            Hawk.put(PREF_ADMOB_REMOVE_ADS, apiObj.getStringVal(API_ADMOB_REMOVE_ADS))
            Hawk.put(PREF_PLAN_VIDEO_DURATION, apiObj.getLongVal(API_PLAN_VIDEO_DURATION))

            val banksArr = apiObj.getJSONArrayVal(API_BANKS)

            if (banksArr != null && banksArr.length() > 0) {

                for (i in 0 until banksArr.length()) {
                    val accountObj = banksArr.getJSONObject(i)

                    val bank = Bank()
                    bank.id = accountObj.getIntVal(API_ID)
                    bank.name = accountObj.getStringVal(API_NAME)
                    bank.code = accountObj.getStringVal(API_CODE)

                    executeTransaction {
                        copyToRealmOrUpdate(bank)
                    }
                }

                return true
            }
        }
    }

    return false
}

fun Realm?.saveAccounts(result: Result<String, FuelError>): Boolean {
    val (data, error) = result

    if (this != null && !this.isClosed && error == null) {
        val apiObj = data.getValidJSONObject()

        if (apiObj.getBooleanVal(API_SUCCESS)) {

            val accountsArr = apiObj.getJSONArrayVal(API_ACCOUNTS)

            if (accountsArr != null && accountsArr.length() > 0) {

                for (i in 0 until accountsArr.length()) {
                    val accountObj = accountsArr.getJSONObject(i)

                    val account = Account()

                    account.id = accountObj.getLongVal(API_ID)
                    account.bankId = accountObj.getIntVal(API_BANK_ID)
                    account.label = accountObj.getStringVal(API_LABEL)
                    account.pixCode = accountObj.getStringVal(API_PIX_CODE)
                    account.agency = accountObj.getStringVal(API_AGENCY)
                    account.account = accountObj.getStringVal(API_ACCOUNT)
                    account.type = accountObj.getStringVal(API_TYPE)
                    account.holder = accountObj.getStringVal(API_HOLDER)
                    account.document = accountObj.getStringVal(API_DOCUMENT)
                    account.legalAccount = accountObj.getBooleanVal(API_LEGAL_ACCOUNT)
                    account.operation = accountObj.getStringVal(API_OPERATION)
                    account.created = accountObj.getStringVal(API_CREATED)
                    account.updated = accountObj.getStringVal(API_UPDATED)
                    account.deleted = accountObj.getStringNullable(API_DELETED)
                    account.synced = true

                    account.bank = where(Bank::class.java)
                        .equalTo("id", account.bankId)
                        .findFirst()

                    executeTransaction {
                        copyToRealmOrUpdate(account)
                    }
                }

                Hawk.put(PREF_LOGGED, true)
            }

            return true
        }
    }

    return false
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    if (this.requestFocus()) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun String?.getNumbers(): String {
    if (this != null && this != "null") {
        return this.replace("\\D".toRegex(), "")
    }
    return ""
}

fun currentTimestamp(): String {
    return SimpleDateFormat(FORMAT_DATETIME_API, BRAZIL).format(Date())
}

fun Realm.unsentAccountsCount(): Long {
    return this.where(Account::class.java)
        .equalTo("synced", false)
        .count()
}

@Suppress("unused")
fun encode64(text: String): String {
    val data = text.toByteArray(Charsets.UTF_8)
    return Base64.encodeToString(data, Base64.DEFAULT)
}

fun decode64(base64: String): String {
    val data = Base64.decode(base64, Base64.DEFAULT)
    return String(data, Charsets.UTF_8)
}

fun View.isVisible(isVisible: Boolean) {
    if (isVisible) show() else hide()
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun String?.isNumeric(): Boolean {
    if (this == null) return false
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return this.matches(regex)
}

fun String?.isNotNumeric(): Boolean = !this.isNumeric()

fun AutoCompleteTextView.setEmpty() = this.text?.clear()

fun AppCompatEditText.setEmpty() = this.text?.clear()

fun isDebug() = BuildConfig.DEBUG