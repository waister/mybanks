package com.duduapps.mybanks.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.util.*
import com.github.kittinunf.fuel.httpPost
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_send_feedback.*
import kotlinx.android.synthetic.main.inc_progress_light.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class SendFeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_feedback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        et_name.setText(Hawk.get(PREF_NAME, ""))
        et_email.setText(Hawk.get(PREF_EMAIL, ""))
        et_comments.setText(Hawk.get(PREF_COMMENTS, ""))

        et_comments.requestFocus()
        et_comments.setOnEditorActionListener(TextView.OnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) {
                sendOpinion()
                return@OnEditorActionListener true
            }
            false
        })
        bt_send_feedback.setOnClickListener { sendOpinion() }
    }

    private fun sendOpinion() {
        val name = et_name.text.toString()
        val email = et_email.text.toString()
        val comments = et_comments.text.toString()

        val errorRes = when {
            name.isEmpty() -> R.string.error_name
            email.isEmpty() -> R.string.error_email
            comments.isEmpty() -> R.string.error_comments
            else -> 0
        }

        if (errorRes > 0) {

            alert(errorRes, R.string.ops) { okButton {} }.show()

        } else {

            rl_progress_light.visibility = View.VISIBLE
            tv_progress_message.setText(R.string.sending_feedback)

            val params = listOf(
                API_NAME to name,
                API_EMAIL to email,
                API_COMMENTS to comments
            )

            API_ROUTE_FEEDBACK_SEND.httpPost(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                rl_progress_light.visibility = View.GONE

                val (data, error) = result

                var success = false
                var message = ""

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    success = apiObj.getBooleanVal(API_SUCCESS)
                    message = apiObj.getStringVal(API_MESSAGE)

                    if (success) {
                        Hawk.delete(PREF_NAME)
                        Hawk.delete(PREF_EMAIL)
                        Hawk.delete(PREF_COMMENTS)

                        et_comments.setText("")

                        alert(message, getString(R.string.success)) {
                            okButton { finish() }
                            onCancelled { finish() }
                        }.show()
                    }
                }

                if (!success) {
                    if (message.isEmpty())
                        message = getString(R.string.error_connection)

                    alert(message, getString(R.string.ops)) { okButton {} }.show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Hawk.put(PREF_NAME, et_name.text.toString())
        Hawk.put(PREF_EMAIL, et_email.text.toString())
        Hawk.put(PREF_COMMENTS, et_comments.text.toString())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
