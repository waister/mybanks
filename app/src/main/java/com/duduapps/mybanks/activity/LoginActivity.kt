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
import com.duduapps.mybanks.databinding.ActivityLoginBinding
import com.duduapps.mybanks.util.API_EMAIL
import com.duduapps.mybanks.util.API_IDENTIFIER
import com.duduapps.mybanks.util.API_MESSAGE
import com.duduapps.mybanks.util.API_ROUTE_EMAIL_SEND_CODE
import com.duduapps.mybanks.util.API_SUCCESS
import com.duduapps.mybanks.util.API_VERIFIER
import com.duduapps.mybanks.util.PREF_DEVICE_ID_OLD
import com.duduapps.mybanks.util.PREF_LOGGED
import com.duduapps.mybanks.util.decode64
import com.duduapps.mybanks.util.getBooleanVal
import com.duduapps.mybanks.util.getStringVal
import com.duduapps.mybanks.util.getValidJSONObject
import com.duduapps.mybanks.util.hide
import com.duduapps.mybanks.util.hideKeyboard
import com.duduapps.mybanks.util.printFuelLog
import com.duduapps.mybanks.util.show
import com.duduapps.mybanks.util.showKeyboard
import com.github.kittinunf.fuel.httpPost
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orhanobut.hawk.Hawk

class LoginActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    private lateinit var binding: ActivityLoginBinding

    private var userEmail: String = ""
    private var apiCode: String = ""
    private var apiIdentifier: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setOnEditorActionListener(this)
        binding.etCode.setOnEditorActionListener(this)

        setupViews()
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

    private fun setupViews() = with(binding) {
        etEmail.show()
        etCode.hide()

        btPositive.setText(R.string.receive_code)

        btPositive.setOnClickListener {
            if (etEmail.visibility == View.VISIBLE)
                sendCode()
            else
                confirmCode()
        }

        btNegative.setOnClickListener {
            finish()
        }

        progress.rlProgressDark.hide()
        progress.tvProgressMessage.setText(R.string.sending)
    }

    private fun sendCode() = with(binding) {
        userEmail = etEmail.text.toString()

        progress.rlProgressDark.show()
        progress.rlProgressDark.hideKeyboard()

        val params = listOf(API_EMAIL to userEmail)
        val t = 1000 * 60

        API_ROUTE_EMAIL_SEND_CODE.httpPost(params).timeout(t).timeoutRead(t)
            .responseString { request, response, result ->
                printFuelLog(request, response, result)

                progress.rlProgressDark.hide()

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
                        MaterialAlertDialogBuilder(applicationContext)
                            .setTitle(R.string.success)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                etCode.requestFocus()
                                etCode.showKeyboard()
                            }
                            .create()
                            .show()

                        etEmail.visibility = View.GONE
                        etCode.visibility = View.VISIBLE

                        btPositive.setText(R.string.confirm_code)

                    }
                }

                if (!success) {
                    if (message.isEmpty())
                        message = getString(R.string.error_unknown)

                    MaterialAlertDialogBuilder(applicationContext)
                        .setTitle(R.string.ops)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
    }

    private fun confirmCode() = with(binding) {
        etCode.hideKeyboard()

        if (etCode.unMasked == apiCode) {

            Hawk.put(PREF_DEVICE_ID_OLD, apiIdentifier)
            Hawk.put(PREF_LOGGED, true)

            CustomApplication().updateFuelParams()

            MaterialAlertDialogBuilder(applicationContext)
                .setTitle(R.string.congratulations)
                .setMessage(R.string.success_email_verified)
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { _, _ ->
                    sendSuccess()
                }
                .create()
                .show()

        } else {

            MaterialAlertDialogBuilder(applicationContext)
                .setTitle(R.string.ops)
                .setMessage(getString(R.string.error_validate_email, userEmail))
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { _, _ ->
                    etCode.requestFocus()
                    etCode.showKeyboard()
                }
                .create()
                .show()

        }
    }

    private fun sendSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

}
