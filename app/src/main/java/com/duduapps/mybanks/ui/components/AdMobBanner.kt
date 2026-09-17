package com.duduapps.mybanks.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import org.koin.compose.koinInject

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    preferencesRepository: PreferencesRepository? = if (LocalInspectionMode.current) null else koinInject(),
) {
    if (LocalInspectionMode.current) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AdMob Banner Preview",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val prefs = preferencesRepository ?: return
    if (prefs.havePlan()) return

    val actualAdUnitId = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/6300978111"
    } else {
        prefs.adMobAdMainId
    }
    if (actualAdUnitId.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier.wrapContentSize(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(adSize)
                        this.adUnitId = actualAdUnitId
                        if (BuildConfig.DEBUG) {
                            adListener = object : AdListener() {
                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                    Log.e(
                                        "AdMobBanner",
                                        "Failed to load banner ad: ${loadAdError.message} (code: ${loadAdError.code})",
                                    )
                                }
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
            )
        }
    }
}
