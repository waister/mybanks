package com.duduapps.mybanks.features.removeads

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.R
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.ui.components.AppTopBar
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun RemoveAdsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RemoveAdsViewModel = koinViewModel(),
    preferencesRepository: PreferencesRepository = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val adUnitId = preferencesRepository.adMobRemoveAds
    val actualAdUnitId = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/5224354917"
    } else {
        adUnitId
    }

    LaunchedEffect(actualAdUnitId) {
        if (actualAdUnitId.isNotEmpty()) {
            RewardedAd.load(
                context,
                actualAdUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        viewModel.onAdLoaded(true)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        viewModel.onAdLoaded(false)
                    }
                },
            )
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is RemoveAdsEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is RemoveAdsEvent.ShowRewardedAd -> {
                    (context as? Activity)?.let { activity ->
                        rewardedAd?.show(activity) {
                            viewModel.onUserEarnedReward()
                        }
                    }
                }
                is RemoveAdsEvent.AdRewardSuccess -> {
                    showSuccessDialog = true
                }
            }
        }
    }

    RemoveAdsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onWatchAdClicked = viewModel::onWatchAdClicked,
        showSuccessDialog = showSuccessDialog,
        onDismissSuccessDialog = {
            showSuccessDialog = false
            onNavigateBack()
        },
    )
}

@Composable
internal fun RemoveAdsScreenContent(
    uiState: RemoveAdsUiState,
    onNavigateBack: () -> Unit,
    onWatchAdClicked: () -> Unit,
    showSuccessDialog: Boolean = false,
    onDismissSuccessDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.remove_adas),
                onBackClick = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.watch_to_by_phrase),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val bodyText = if (uiState.hasPlan) {
                stringResource(R.string.watch_to_by_body_paid, uiState.remainingDays)
            } else {
                stringResource(R.string.watch_to_by_body, uiState.remainingDays)
            }

            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            val buttonText = if (uiState.hasPlan) {
                stringResource(R.string.watch_to_by_button_again)
            } else {
                stringResource(R.string.watch_to_by_button)
            }

            Button(
                onClick = onWatchAdClicked,
                enabled = uiState.isAdLoaded,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                Text(text = buttonText)
            }

            if (!uiState.isAdLoaded) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Carregando vídeo...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissSuccessDialog,
            title = { Text(stringResource(R.string.plan_success_title)) },
            text = { Text(stringResource(R.string.plan_success_body)) },
            confirmButton = {
                TextButton(onClick = onDismissSuccessDialog) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}

@Preview(name = "Remove Ads - Free Plan (Ad Loaded)", showBackground = true)
@Composable
private fun RemoveAdsFreePlanAdLoadedPreview() {
    MyBanksTheme {
        RemoveAdsScreenContent(
            uiState = RemoveAdsPreviewsData.freePlanAdLoadedState,
            onNavigateBack = {},
            onWatchAdClicked = {},
        )
    }
}

@Preview(name = "Remove Ads - Free Plan (Ad Loading)", showBackground = true)
@Composable
private fun RemoveAdsFreePlanAdLoadingPreview() {
    MyBanksTheme {
        RemoveAdsScreenContent(
            uiState = RemoveAdsPreviewsData.freePlanAdLoadingState,
            onNavigateBack = {},
            onWatchAdClicked = {},
        )
    }
}

@Preview(name = "Remove Ads - Active Plan", showBackground = true)
@Composable
private fun RemoveAdsActivePlanPreview() {
    MyBanksTheme {
        RemoveAdsScreenContent(
            uiState = RemoveAdsPreviewsData.activePlanState,
            onNavigateBack = {},
            onWatchAdClicked = {},
        )
    }
}
