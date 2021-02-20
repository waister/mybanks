@file:Suppress("unused")

package com.duduapps.mybanks.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val APP_HOST = "https://duduapps.com/"
const val API_ROOT = "https://duduapps.com/api/mybanks"

const val API_ROUTE_BANKS = "/banks"
const val API_ROUTE_ACCOUNTS = "/accounts"
const val API_ROUTE_ACCOUNT_REGISTER = "/account/register"
const val API_ROUTE_IDENTIFY = "/identify"
const val API_ROUTE_EMAIL_SEND_CODE = "/email-send-code"
const val API_ROUTE_FEEDBACK_SEND = "/message/send"

const val API_ANDROID = "android"
const val API_IDENTIFIER = "identifier"
const val API_VERSION = "version"
const val API_PLATFORM = "platform"
const val API_DEBUG = "debug"
const val API_V = "api_v"
const val API_VERSION_LAST = "version_last"
const val API_VERSION_MIN = "version_min"
const val API_SUCCESS = "success"
const val API_MESSAGE = "message"
const val API_BANKS = "banks"
const val API_ACCOUNTS = "accounts"
const val API_ID = "id"
const val API_LABEL = "label"
const val API_BANK_ID = "bank_id"
const val API_AGENCY = "agency"
const val API_ACCOUNT = "account"
const val API_TYPE = "type"
const val API_HOLDER = "holder"
const val API_DOCUMENT = "document"
const val API_YES = "yes"
const val API_NO = "no"
const val API_LEGAL_ACCOUNT = "legal_account"
const val API_OPERATION = "operation"
const val API_NAME = "name"
const val API_CODE = "code"
const val API_VERIFIER = "verifier"
const val API_CREATED = "created_at"
const val API_UPDATED = "updated_at"
const val API_DELETED = "deleted_at"
const val API_PIX_CODE = "pix_code"
const val API_TOKEN = "token"
const val API_WAKEUP = "wakeup"
const val API_SHARE_LINK = "store_link"
const val API_APP_NAME = "app_name"
const val API_ADMOB_ID = "admob_id"
const val API_ADMOB_AD_MAIN_ID = "admob_ad_main_id"
const val API_ADMOB_INTERSTITIAL_ID = "admob_interstitial_id"
const val API_ADMOB_REMOVE_ADS = "admob_remove_ads"
const val API_EMAIL = "email"
const val API_COMMENTS = "comments"
const val API_PLAN_VIDEO_DURATION = "plan_video_duration"
const val API_INTERSTITIAL_MIN_INTERVAL = "interstitial_min_interval"

fun String?.getValidJSONObject(): JSONObject? {
    if (this != null && this.isNotEmpty() && this != "null") {
        try {
            return JSONObject(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun String?.getValidJSONArray(): JSONArray? {
    if (this != null && this.isNotEmpty() && this != "null") {
        try {
            return JSONArray(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONObjectVal(tag: String): JSONObject? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONObject(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONArrayVal(tag: String): JSONArray? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONArray(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun String?.getStringValid(): String {
    if (this != null && this.isNotEmpty() && this != "null" && this != "[null]") {
        return this
    }
    return ""
}

fun JSONObject?.getArrayValid(tag: String): JSONArray? {
    if (this != null) {
        try {
            return getJSONArray(tag)
        } catch (e: JSONException) {
        }
    }
    return null
}

fun JSONObject?.getObjectValid(tag: String): JSONObject? {
    if (this != null) {
        try {
            return getJSONObject(tag)
        } catch (e: JSONException) {
        }
    }
    return null
}

fun JSONObject?.getStringVal(tag: String, default: String = ""): String {
    if (this != null && has(tag)) {
        try {
            return getString(tag).getStringValid()
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getStringNullable(tag: String): String? {
    val value = getStringVal(tag)
    return if (value.isNotEmpty()) value else null
}

fun JSONObject?.getIntVal(tag: String, default: Int = 0): Int {
    if (this != null && has(tag)) {
        try {
            return getInt(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getLongVal(tag: String, default: Long = 0): Long {
    if (this != null && has(tag)) {
        try {
            return getLong(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getDoubleVal(tag: String, default: Double = 0.0): Double {
    if (this != null && has(tag)) {
        try {
            return getDouble(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getBooleanVal(tag: String, default: Boolean = false): Boolean {
    if (this != null && has(tag)) {
        try {
            return getBoolean(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}
