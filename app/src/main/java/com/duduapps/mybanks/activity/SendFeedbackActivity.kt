package com.duduapps.mybanks.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.databinding.ActivitySendFeedbackBinding
import com.duduapps.mybanks.util.API_COMMENTS
import com.duduapps.mybanks.util.API_EMAIL
import com.duduapps.mybanks.util.API_MESSAGE
import com.duduapps.mybanks.util.API_NAME
import com.duduapps.mybanks.util.API_ROUTE_FEEDBACK_SEND
import com.duduapps.mybanks.util.API_SUCCESS
import com.duduapps.mybanks.util.PREF_COMMENTS
import com.duduapps.mybanks.util.PREF_EMAIL
import com.duduapps.mybanks.util.PREF_NAME
import com.duduapps.mybanks.util.getBooleanVal
import com.duduapps.mybanks.util.getStringVal
import com.duduapps.mybanks.util.getValidJSONObject
import com.duduapps.mybanks.util.printFuelLog
import com.duduapps.mybanks.util.setEmpty
import com.github.kittinunf.fuel.httpPost
import com.orhanobut.hawk.Hawk
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class SendFeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySendFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.etName.setText(Hawk.get(PREF_NAME, ""))
        binding.etEmail.setText(Hawk.get(PREF_EMAIL, ""))
        binding.etComments.setText(Hawk.get(PREF_COMMENTS, ""))

        binding.etComments.requestFocus()
        binding.etComments.setOnEditorActionListener(TextView.OnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) {
                sendOpinion()
                return@OnEditorActionListener true
            }
            false
        })
        binding.btSendFeedback.setOnClickListener { sendOpinion() }
    }

    private fun sendOpinion() = with(binding) {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val comments = etComments.text.toString()

        val errorRes = when {
            name.isEmpty() -> R.string.error_name
            email.isEmpty() -> R.string.error_email
            comments.isEmpty() -> R.string.error_comments
            else -> 0
        }

        if (errorRes > 0) {

            alert(errorRes, R.string.ops) { okButton {} }.show()

        } else {

            progress.rlProgressLight.visibility = View.VISIBLE
            progress.tvProgressMessage.setText(R.string.sending_feedback)

            val params = listOf(
                API_NAME to name,
                API_EMAIL to email,
                API_COMMENTS to comments
            )

            API_ROUTE_FEEDBACK_SEND.httpPost(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                progress.rlProgressLight.visibility = View.GONE

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

                        etComments.setEmpty()

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

        Hawk.put(PREF_NAME, binding.etName.text.toString())
        Hawk.put(PREF_EMAIL, binding.etEmail.text.toString())
        Hawk.put(PREF_COMMENTS, binding.etComments.text.toString())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
