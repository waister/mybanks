package com.duduapps.mybanks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duduapps.mybanks.navigation.AppNavHost
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import com.duduapps.mybanks.utils.AppOpenAdManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val appOpenAdManager: AppOpenAdManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyBanksTheme {
                AppNavHost()
            }
        }

        appOpenAdManager.showAdIfAvailable(this)
    }
}
