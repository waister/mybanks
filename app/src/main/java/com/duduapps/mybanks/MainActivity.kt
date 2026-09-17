package com.duduapps.mybanks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.duduapps.mybanks.data.repository.AppConfigRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.navigation.AppNavHost
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import com.duduapps.mybanks.utils.AppOpenAdManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val appOpenAdManager: AppOpenAdManager by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val appConfigRepository: AppConfigRepository by inject()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Permission result handled; UI updates reactively via lifecycle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        initFcmToken()

        setContent {
            MyBanksTheme {
                AppNavHost()
            }
        }

        appOpenAdManager.showAdIfAvailable(this)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun initFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (!token.isNullOrEmpty()) {
                    preferencesRepository.fcmToken = token
                    lifecycleScope.launch {
                        appConfigRepository.identify(token)
                    }
                }
            }
        }
    }
}
