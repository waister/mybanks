package com.duduapps.mybanks.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.duduapps.mybanks.databinding.ActivityAlertLoginBinding
import com.duduapps.mybanks.util.PREF_SHOW_ALERT_LOGIN
import com.orhanobut.hawk.Hawk

class AlertLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlertLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btPositive.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btNegative.setOnClickListener {
            finish()
        }

        binding.btNeverShow.setOnClickListener {
            Hawk.put(PREF_SHOW_ALERT_LOGIN, false)
            finish()
        }
    }

}
