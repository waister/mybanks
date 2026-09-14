package com.duduapps.mybanks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import org.koin.compose.koinInject

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    preferencesRepository: PreferencesRepository? = if (LocalInspectionMode.current) null else koinInject(),
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "AdMob Banner Preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val prefs = preferencesRepository ?: return
    if (prefs.havePlan()) return

    val adUnitId = prefs.adMobAdMainId
    if (adUnitId.isEmpty()) return

    val actualAdUnitId = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/6300978111"
    } else {
        adUnitId
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = actualAdUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
