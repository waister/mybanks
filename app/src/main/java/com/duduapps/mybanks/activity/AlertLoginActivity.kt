package com.duduapps.mybanks.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.util.PREF_SHOW_ALERT_LOGIN
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_alert_login.*
import org.jetbrains.anko.intentFor

class AlertLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_login)

        bt_positive.setOnClickListener {
            startActivity(intentFor<LoginActivity>())
            finish()
        }

        bt_negative.setOnClickListener {
            finish()
        }

        bt_never_show.setOnClickListener {
            Hawk.put(PREF_SHOW_ALERT_LOGIN, false)
            finish()
        }
    }

}
