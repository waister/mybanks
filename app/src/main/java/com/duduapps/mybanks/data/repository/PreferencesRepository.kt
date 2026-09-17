package com.duduapps.mybanks.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import kotlin.random.Random

interface PreferencesRepository {
    var deviceId: String
    var deviceIdOld: String
    var isLogged: Boolean
    var fcmToken: String
    var storeLink: String
    var appName: String
    var lastHolder: String
    var lastDocument: String
    var lastLegalAccount: Boolean
    var showAlertLogin: Boolean
    var name: String
    var email: String
    var comments: String
    var adMobId: String
    var adMobAdMainId: String
    var adMobInterstitialId: String
    var adMobOpenAppId: String
    var adMobRemoveAds: String
    var planVideoDuration: Long
    var planVideoMillis: Long

    fun havePlan(): Boolean
    fun ensureDeviceId(): String
    fun logout()
    fun clearFeedbackCache()
}

class PreferencesRepositoryImpl(
    private val context: Context,
) : PreferencesRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "mybanks_prefs"

        private const val KEY_DEVICE_ID = "PrefDeviceId"
        private const val KEY_DEVICE_ID_OLD = "PrefIdentifier"
        private const val KEY_LOGGED = "PrefLogged"
        private const val KEY_FCM_TOKEN = "PrefFcmToken"
        private const val KEY_STORE_LINK = "PrefStoreLink"
        private const val KEY_APP_NAME = "PrefAppName"
        private const val KEY_LAST_HOLDER = "PrefLastHolder"
        private const val KEY_LAST_DOCUMENT = "PrefLastDocument"
        private const val KEY_LAST_LEGAL_ACCOUNT = "PrefLastLegalAccount"
        private const val KEY_SHOW_ALERT_LOGIN = "PrefShowAlertLogin"
        private const val KEY_NAME = "PrefName"
        private const val KEY_EMAIL = "PrefEmail"
        private const val KEY_COMMENTS = "PrefComments"
        private const val KEY_ADMOB_ID = "PrefAdMobId"
        private const val KEY_ADMOB_AD_MAIN_ID = "PrefAdMobAdMainId"
        private const val KEY_ADMOB_INTERSTITIAL_ID = "PrefAdMobInterstitialId"
        private const val KEY_ADMOB_OPEN_APP_ID = "PrefAdMobOpenAppId"
        private const val KEY_ADMOB_REMOVE_ADS = "PrefAdMobRemoveAds"
        private const val KEY_PLAN_VIDEO_DURATION = "PrefPlanVideoDuration"
        private const val KEY_PLAN_VIDEO_MILLIS = "PrefPlanVideoMillis"

        private const val ONE_DAY_MILLIS: Long = 24 * 60 * 60 * 1000L
    }

    override var deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    override var deviceIdOld: String
        get() = prefs.getString(KEY_DEVICE_ID_OLD, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_DEVICE_ID_OLD, value).apply()

    override var isLogged: Boolean
        get() = prefs.getBoolean(KEY_LOGGED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOGGED, value).apply()

    override var fcmToken: String
        get() = prefs.getString(KEY_FCM_TOKEN, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()

    override var storeLink: String
        get() = prefs.getString(KEY_STORE_LINK, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_STORE_LINK, value).apply()

    override var appName: String
        get() = prefs.getString(KEY_APP_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_APP_NAME, value).apply()

    override var lastHolder: String
        get() = prefs.getString(KEY_LAST_HOLDER, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_LAST_HOLDER, value).apply()

    override var lastDocument: String
        get() = prefs.getString(KEY_LAST_DOCUMENT, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_LAST_DOCUMENT, value).apply()

    override var lastLegalAccount: Boolean
        get() = prefs.getBoolean(KEY_LAST_LEGAL_ACCOUNT, false)
        set(value) = prefs.edit().putBoolean(KEY_LAST_LEGAL_ACCOUNT, value).apply()

    override var showAlertLogin: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ALERT_LOGIN, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ALERT_LOGIN, value).apply()

    override var name: String
        get() = prefs.getString(KEY_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    override var email: String
        get() = prefs.getString(KEY_EMAIL, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    override var comments: String
        get() = prefs.getString(KEY_COMMENTS, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_COMMENTS, value).apply()

    override var adMobId: String
        get() = prefs.getString(KEY_ADMOB_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_ADMOB_ID, value).apply()

    override var adMobAdMainId: String
        get() = prefs.getString(KEY_ADMOB_AD_MAIN_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_ADMOB_AD_MAIN_ID, value).apply()

    override var adMobInterstitialId: String
        get() = prefs.getString(KEY_ADMOB_INTERSTITIAL_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_ADMOB_INTERSTITIAL_ID, value).apply()

    override var adMobOpenAppId: String
        get() = prefs.getString(KEY_ADMOB_OPEN_APP_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_ADMOB_OPEN_APP_ID, value).apply()

    override var adMobRemoveAds: String
        get() = prefs.getString(KEY_ADMOB_REMOVE_ADS, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_ADMOB_REMOVE_ADS, value).apply()

    override var planVideoDuration: Long
        get() = prefs.getLong(KEY_PLAN_VIDEO_DURATION, ONE_DAY_MILLIS)
        set(value) = prefs.edit().putLong(KEY_PLAN_VIDEO_DURATION, value).apply()

    override var planVideoMillis: Long
        get() = prefs.getLong(KEY_PLAN_VIDEO_MILLIS, 0L)
        set(value) = prefs.edit().putLong(KEY_PLAN_VIDEO_MILLIS, value).apply()

    override fun havePlan(): Boolean {
        if (planVideoMillis != 0L) {
            val duration = if (planVideoDuration > 0) planVideoDuration else ONE_DAY_MILLIS
            val expiration = planVideoMillis + duration
            return expiration > System.currentTimeMillis()
        }
        return false
    }

    override fun ensureDeviceId(): String {
        val currentId = deviceId
        val isNumeric = currentId.isNotEmpty() && currentId.all { it.isDigit() }
        if (!isLogged && (!isNumeric || currentId.isEmpty())) {
            if (currentId.isNotEmpty() && !isNumeric) {
                deviceIdOld = currentId
            }
            val millis = Calendar.getInstance().timeInMillis.toString()
            val random = Random.nextInt(10000, 99999)
            var stringId = "$millis$random"
            stringId = when {
                stringId.length > 18 -> stringId.substring(0, 18)
                stringId.length < 18 -> stringId.padEnd(18, '9')
                else -> stringId
            }
            deviceId = stringId
            return stringId
        }
        return currentId
    }

    override fun logout() {
        prefs.edit().remove(KEY_LOGGED).apply()
    }

    override fun clearFeedbackCache() {
        prefs.edit()
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_COMMENTS)
            .apply()
    }
}
