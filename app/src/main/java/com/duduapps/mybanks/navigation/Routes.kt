package com.duduapps.mybanks.navigation

object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val ACCOUNT_DETAIL = "account_detail/{accountId}"
    const val ACCOUNT_FORM = "account_form?accountId={accountId}"
    const val LOGIN = "login"
    const val REMOVE_ADS = "remove_ads"
    const val FEEDBACK = "feedback"

    fun accountDetail(accountId: Long): String = "account_detail/$accountId"
    fun accountForm(accountId: Long? = null): String {
        return if (accountId != null && accountId > 0) {
            "account_form?accountId=$accountId"
        } else {
            "account_form"
        }
    }
}
