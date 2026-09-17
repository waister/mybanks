package com.duduapps.mybanks.features.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duduapps.mybanks.R
import com.duduapps.mybanks.features.main.components.AccountItem
import com.duduapps.mybanks.features.main.components.EmptyAccountsView
import com.duduapps.mybanks.features.main.components.NotificationPermissionBanner
import com.duduapps.mybanks.ui.components.AdMobBanner
import com.duduapps.mybanks.ui.components.ConfirmDialog
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import com.duduapps.mybanks.utils.InterstitialAdManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccountDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRemoveAds: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    viewModel: MainViewModel = koinViewModel(),
    interstitialAdManager: InterstitialAdManager = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val shareAccountsTitle = stringResource(R.string.my_bank_accounts)
    val shareAppSubject = stringResource(R.string.share_subject)
    val shareAppTextTemplate = stringResource(R.string.share_text)

    var areNotificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var isNotificationBannerDismissed by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        areNotificationsEnabled = isGranted || NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val onEnableNotifications = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openAppNotificationSettings(context)
            }
        } else {
            openAppNotificationSettings(context)
        }
    }

    LifecycleResumeEffect(Unit) {
        areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        interstitialAdManager.loadAd(context)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is MainEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is MainEvent.CopyToClipboard -> {
                    val clip = android.content.ClipData.newPlainText("accounts", event.text).toClipEntry()
                    clipboard.setClipEntry(clip)
                }

                is MainEvent.ShareAccounts -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(intent, shareAccountsTitle))
                }

                is MainEvent.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, event.url.toUri())
                    context.startActivity(intent)
                }

                is MainEvent.ShowInterstitialAd -> {
                    (context as? Activity)?.let { activity ->
                        interstitialAdManager.showAd(activity)
                    }
                }
            }
        }
    }

    MainScreenContent(
        uiState = uiState,
        onNavigateToAddAccount = onNavigateToAddAccount,
        onNavigateToAccountDetail = onNavigateToAccountDetail,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRemoveAds = onNavigateToRemoveAds,
        onNavigateToFeedback = onNavigateToFeedback,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onToggleSearch = viewModel::toggleSearch,
        onCopyAllClick = viewModel::onCopyAllClicked,
        onShareAllClick = viewModel::onShareAllClicked,
        onLogout = viewModel::onLogout,
        onDismissLoginAlert = viewModel::onDismissLoginAlert,
        onDismissUpdateDialog = viewModel::dismissUpdateDialog,
        onUpdateClick = { url ->
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        },
        onShareApp = { shareApp(context, shareAppSubject, shareAppTextTemplate) },
        onRateOnStore = { openPlayStore(context) },
        showNotificationWarning = !areNotificationsEnabled && !isNotificationBannerDismissed,
        onEnableNotifications = onEnableNotifications,
        onDismissNotificationWarning = { isNotificationBannerDismissed = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenContent(
    uiState: MainUiState,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccountDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRemoveAds: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onCopyAllClick: () -> Unit,
    onShareAllClick: () -> Unit,
    onLogout: () -> Unit,
    onDismissLoginAlert: (Boolean) -> Unit,
    onDismissUpdateDialog: () -> Unit,
    onUpdateClick: (String) -> Unit,
    onShareApp: () -> Unit,
    onRateOnStore: () -> Unit,
    showNotificationWarning: Boolean = false,
    onEnableNotifications: () -> Unit = {},
    onDismissNotificationWarning: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSearchActive) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = uiState.searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { onToggleSearch(false) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Fechar busca")
                                }
                            },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {}
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    actions = {
                        if (uiState.accounts.size > 3) {
                            IconButton(onClick = { onToggleSearch(true) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar contas")
                            }
                        }
                        IconButton(onClick = onNavigateToAddAccount) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar conta")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            if (uiState.accounts.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.copy_accounts)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onCopyAllClick()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share_accounts)) },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onShareAllClick()
                                    },
                                )
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_ads)) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToRemoveAds()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_app)) },
                                onClick = {
                                    showMenu = false
                                    onShareApp()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.rate_on_store)) },
                                onClick = {
                                    showMenu = false
                                    onRateOnStore()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.send_feedback)) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToFeedback()
                                },
                            )
                            if (!uiState.isLogged) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_login)) },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToLogin()
                                    },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.logout_account)) },
                                    onClick = {
                                        showMenu = false
                                        showLogoutDialog = true
                                    },
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            AdMobBanner()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (showNotificationWarning) {
                NotificationPermissionBanner(
                    onEnableClick = onEnableNotifications,
                    onDismissClick = onDismissNotificationWarning,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (uiState.filteredAccounts.isEmpty()) {
                    EmptyAccountsView(
                        isLogged = uiState.isLogged,
                        onAddAccountClick = onNavigateToAddAccount,
                        onLoginClick = onNavigateToLogin,
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.filteredAccounts,
                            key = { it.id },
                        ) { account ->
                            AccountItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = stringResource(R.string.logout_account),
            message = stringResource(R.string.confirm_logout_account),
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false },
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
        )
    }

    if (uiState.showAlertLogin) {
        AlertDialog(
            onDismissRequest = { onDismissLoginAlert(false) },
            title = { Text(stringResource(R.string.active_sync_title)) },
            text = { Text(stringResource(R.string.active_sync_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissLoginAlert(false)
                        onNavigateToLogin()
                    },
                ) {
                    Text(stringResource(R.string.active_sync_positive))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onDismissLoginAlert(true) }) {
                        Text(stringResource(R.string.never_show_again))
                    }
                    TextButton(onClick = { onDismissLoginAlert(false) }) {
                        Text(stringResource(R.string.active_sync_negative))
                    }
                }
            },
        )
    }

    if (uiState.isUpdateDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isMandatoryUpdate) onDismissUpdateDialog()
            },
            title = { Text(stringResource(R.string.updated_title)) },
            text = {
                Text(
                    if (uiState.isMandatoryUpdate) {
                        stringResource(R.string.update_needed)
                    } else {
                        stringResource(R.string.update_available)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { onUpdateClick(uiState.updateStoreUrl) }) {
                    Text(stringResource(R.string.updated_positive))
                }
            },
            dismissButton = {
                if (!uiState.isMandatoryUpdate) {
                    TextButton(onClick = onDismissUpdateDialog) {
                        Text(stringResource(R.string.updated_negative))
                    }
                }
            },
        )
    }
}

private fun shareApp(context: Context, subject: String, textTemplate: String) {
    val storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val text = textTemplate.format(storeUrl)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, subject))
}

private fun openPlayStore(context: Context) {
    val storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val intent = Intent(Intent.ACTION_VIEW, storeUrl.toUri())
    context.startActivity(intent)
}

private fun openAppNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    context.startActivity(intent)
}

@Preview(name = "Main Screen - Populated", showBackground = true)
@Composable
private fun MainScreenPopulatedPreview() {
    MyBanksTheme {
        MainScreenContent(
            uiState = MainPreviewsData.populatedLoggedState,
            onNavigateToAddAccount = {},
            onNavigateToAccountDetail = {},
            onNavigateToLogin = {},
            onNavigateToRemoveAds = {},
            onNavigateToFeedback = {},
            onSearchQueryChange = {},
            onToggleSearch = {},
            onCopyAllClick = {},
            onShareAllClick = {},
            onLogout = {},
            onDismissLoginAlert = {},
            onDismissUpdateDialog = {},
            onUpdateClick = {},
            onShareApp = {},
            onRateOnStore = {},
        )
    }
}

@Preview(name = "Main Screen - Empty", showBackground = true)
@Composable
private fun MainScreenEmptyPreview() {
    MyBanksTheme {
        MainScreenContent(
            uiState = MainPreviewsData.emptyState,
            onNavigateToAddAccount = {},
            onNavigateToAccountDetail = {},
            onNavigateToLogin = {},
            onNavigateToRemoveAds = {},
            onNavigateToFeedback = {},
            onSearchQueryChange = {},
            onToggleSearch = {},
            onCopyAllClick = {},
            onShareAllClick = {},
            onLogout = {},
            onDismissLoginAlert = {},
            onDismissUpdateDialog = {},
            onUpdateClick = {},
            onShareApp = {},
            onRateOnStore = {},
        )
    }
}

@Preview(name = "Main Screen - Searching", showBackground = true)
@Composable
private fun MainScreenSearchingPreview() {
    MyBanksTheme {
        MainScreenContent(
            uiState = MainPreviewsData.searchActiveState,
            onNavigateToAddAccount = {},
            onNavigateToAccountDetail = {},
            onNavigateToLogin = {},
            onNavigateToRemoveAds = {},
            onNavigateToFeedback = {},
            onSearchQueryChange = {},
            onToggleSearch = {},
            onCopyAllClick = {},
            onShareAllClick = {},
            onLogout = {},
            onDismissLoginAlert = {},
            onDismissUpdateDialog = {},
            onUpdateClick = {},
            onShareApp = {},
            onRateOnStore = {},
        )
    }
}
