package com.duduapps.mybanks.activity

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.application.CustomApplication
import com.duduapps.mybanks.util.*
import com.github.kittinunf.fuel.httpPost
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.inc_progress_light.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class LoginActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    private var userEmail: String = ""
    private var apiCode: String = ""
    private var apiIdentifier: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        et_email.visibility = View.VISIBLE
        et_code.visibility = View.GONE

        et_email.setOnEditorActionListener(this)
        et_code.setOnEditorActionListener(this)

        bt_positive.setText(R.string.receive_code)

        bt_positive.setOnClickListener {
            if (et_email.visibility == View.VISIBLE)
                sendCode()
            else
                confirmCode()
        }

        bt_negative.setOnClickListener {
            finish()
        }

        rl_progress.visibility = View.GONE
        tv_progress_message.setText(R.string.sending)
    }

    override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return when (actionId) {
            EditorInfo.IME_ACTION_SEND -> {
                sendCode()
                true
            }
            else -> false
        }
    }

    private fun sendCode() {
        userEmail = et_email.text.toString()

        rl_progress.visibility = View.VISIBLE
        rl_progress.hideKeyboard()

        val params = listOf(API_EMAIL to userEmail)
        val t = 1000 * 60

        API_ROUTE_EMAIL_SEND_CODE.httpPost(params).timeout(t).timeoutRead(t)
            .responseString { request, response, result ->
                printFuelLog(request, response, result)

                rl_progress.visibility = View.GONE

                val (data, error) = result
                var success = false
                var message = ""

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    success = apiObj.getBooleanVal(API_SUCCESS)
                    message = apiObj.getStringVal(API_MESSAGE)
                    apiIdentifier = apiObj.getStringVal(API_IDENTIFIER)
                    apiCode = decode64(apiObj.getStringVal(API_VERIFIER))

                    if (success) {

                        alert(message, getString(R.string.success)) {
                            okButton {
                                et_code.requestFocus()
                                et_code.showKeyboard()
                            }
                        }.show()

                        et_email.visibility = View.GONE
                        et_code.visibility = View.VISIBLE

                        bt_positive.setText(R.string.confirm_code)

                    }
                }

                if (!success) {
                    if (message.isEmpty())
                        message = getString(R.string.error_unknown)

                    alert(message, getString(R.string.ops)) { okButton {} }.show()
                }
            }
    }

    private fun confirmCode() {
        et_code.hideKeyboard()

        if (et_code.unMasked == apiCode) {

            Hawk.put(PREF_IDENTIFIER, apiIdentifier)
            Hawk.put(PREF_LOGGED, true)

            CustomApplication().updateFuelParams()

            alert(R.string.success_email_verified, R.string.congratulations) {
                okButton { sendSuccess() }
                onCancelled { sendSuccess() }
            }.show()

        } else {

            val title = getString(R.string.ops)
            val message = getString(R.string.error_validate_email, userEmail)

            alert(message, title) {
                positiveButton(R.string.correct_code) {
                    et_code.requestFocus()
                    et_code.showKeyboard()
                }
            }.show()

        }
    }

    private fun sendSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

}
