package com.duduapps.mybanks.features.account.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duduapps.mybanks.R
import com.duduapps.mybanks.ui.components.AdMobBanner
import com.duduapps.mybanks.ui.components.AppTopBar
import com.duduapps.mybanks.ui.components.ConfirmDialog
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import com.google.android.gms.ads.AdSize
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRemoveAds: () -> Unit,
    viewModel: AccountDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountDetailEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AccountDetailEvent.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(event.text))
                }
                is AccountDetailEvent.ShareAccount -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.my_bank_account)))
                }
                is AccountDetailEvent.NavigateBack -> currentOnNavigateBack()
            }
        }
    }

    AccountDetailScreenContent(
        uiState = uiState,
        accountId = accountId,
        onNavigateBack = onNavigateBack,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToRemoveAds = onNavigateToRemoveAds,
        onCopyField = viewModel::onCopyField,
        onCopyAll = viewModel::onCopyAll,
        onShareAll = viewModel::onShareAll,
        onDeleteAccount = viewModel::onDeleteAccount,
    )
}

@Composable
internal fun AccountDetailScreenContent(
    uiState: AccountDetailUiState,
    accountId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRemoveAds: () -> Unit,
    onCopyField: (String, String) -> Unit,
    onCopyAll: () -> Unit,
    onShareAll: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = uiState.account?.label ?: stringResource(R.string.account_details),
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(accountId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar conta",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir conta",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove_adas)) },
                            onClick = {
                                showMenu = false
                                onNavigateToRemoveAds()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            AdMobBanner(adSize = AdSize.MEDIUM_RECTANGLE)
        },
    ) { innerPadding ->
        val account = uiState.account
        if (account != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (account.pixCode.isNotEmpty()) {
                    DetailRow(
                        label = "Chave PIX",
                        value = account.pixCode,
                        onClick = { onCopyField(account.pixCode, "Chave PIX") },
                    )
                }

                if (account.bank != null) {
                    DetailRow(
                        label = "Banco",
                        value = "${account.bank.name} (${account.bank.code})",
                        onClick = { onCopyField(account.bank.code, "Código do Banco") },
                    )
                }

                DetailRow(
                    label = "Agência",
                    value = account.agency,
                    onClick = { onCopyField(account.agency, "Agência") },
                )

                DetailRow(
                    label = "Conta",
                    value = account.account,
                    onClick = { onCopyField(account.account, "Conta") },
                )

                if (account.operation.isNotEmpty()) {
                    DetailRow(
                        label = "Operação",
                        value = account.operation,
                        onClick = { onCopyField(account.operation, "Operação") },
                    )
                }

                DetailRow(
                    label = "Tipo",
                    value = account.type,
                    onClick = { onCopyField(account.type, "Tipo") },
                )

                DetailRow(
                    label = "Titular",
                    value = account.holder,
                    onClick = { onCopyField(account.holder, "Titular") },
                )

                val docLabel = if (account.legalAccount) "CNPJ" else "CPF"
                DetailRow(
                    label = docLabel,
                    value = account.document,
                    onClick = { onCopyField(account.document, docLabel) },
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = onCopyAll,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.copy_account))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onShareAll,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.share_account))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.confirmation),
            message = stringResource(R.string.confirm_deleted_account),
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false },
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copiar $label",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp,
        )
    }
}

@Preview(name = "Account Detail - Individual", showBackground = true)
@Composable
private fun AccountDetailIndividualPreview() {
    MyBanksTheme {
        AccountDetailScreenContent(
            uiState = AccountDetailPreviewsData.individualAccountState,
            accountId = 1L,
            onNavigateBack = {},
            onNavigateToEdit = {},
            onNavigateToRemoveAds = {},
            onCopyField = { _, _ -> },
            onCopyAll = {},
            onShareAll = {},
            onDeleteAccount = {},
        )
    }
}

@Preview(name = "Account Detail - Legal Entity", showBackground = true)
@Composable
private fun AccountDetailLegalPreview() {
    MyBanksTheme {
        AccountDetailScreenContent(
            uiState = AccountDetailPreviewsData.legalAccountState,
            accountId = 2L,
            onNavigateBack = {},
            onNavigateToEdit = {},
            onNavigateToRemoveAds = {},
            onCopyField = { _, _ -> },
            onCopyAll = {},
            onShareAll = {},
            onDeleteAccount = {},
        )
    }
}
