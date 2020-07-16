package com.duduapps.mybanks.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.LinearLayout
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.model.Bank
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*


const val PARAM_ID = "ParamId"
const val PARAM_TYPE = "ParamType"
const val PARAM_ITEM_ID = "ParamItemId"

const val ONE_SECOND: Long = 1000
const val ONE_MINUTE: Long = ONE_SECOND * 60
const val ONE_HOUR: Long = ONE_MINUTE * 60
const val ONE_DAY: Long = ONE_HOUR * 24
const val FIVE_DAYS: Long = ONE_DAY * 5

fun isLogged(): Boolean {
    return Hawk.get(PREF_LOGGED, false)
}

fun Context.storeAppLink(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun havePlan(): Boolean {
    val planVideoMillis = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L)
    if (planVideoMillis != 0L) {
        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)
        val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
        return expiration > System.currentTimeMillis()
    }
    return false
}

fun Activity?.createInterstitialAd(): InterstitialAd? {
    var interstitialAd: InterstitialAd? = null

    if (this != null && !havePlan()) {
        val adUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            Hawk.get(PREF_ADMOB_INTERSTITIAL_ID, "")
        }

        if (adUnitId.isNotEmpty()) {
            interstitialAd = InterstitialAd(this)
            interstitialAd.adUnitId = adUnitId
        }
    }

    return interstitialAd
}

fun Activity?.loadAdBanner(root: LinearLayout?, adUnitId: String, adSize: AdSize?) {
    if (this == null || root == null || havePlan()) return

    val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"

    val adView = AdView(this)
    adView.adSize = adSize ?: AdSize.SMART_BANNER
    adView.adUnitId = if (BuildConfig.DEBUG) testAdUnitId else adUnitId

    root.addView(
        adView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    )

    adView.loadAd(AdRequest.Builder().build())
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(getString(R.string.app_name), text)
    clipboard.setPrimaryClip(clip)
}

fun printFuelLog(request: Request, response: Response, result: Result<String, FuelError>) {
    if (BuildConfig.DEBUG) {
        println("\n---------- FUEL_REQUEST_START\n")
        println(request)
        println("\n---------- FUEL_REQUEST_END\n")

        println("\n---------- FUEL_RESPONSE_START\n")
        println(response)
        println("\n---------- FUEL_RESPONSE_END\n")

        println("\n---------- FUEL_RESULT_START\n")
        println(result)
        println("\n---------- FUEL_RESULT_END\n")
    }
}


fun String?.stringToInt(): Int {
    if (this != null && this != "null") {
        val number = this.replace("[^\\d]".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.isValidUrl(): Boolean {
    return this != null && this.isNotEmpty() && URLUtil.isValidUrl(this)
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
            val canvas = Canvas(output!!)

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
                        .findFirst()!!

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
        return this.replace("[^\\d]".toRegex(), "")
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
