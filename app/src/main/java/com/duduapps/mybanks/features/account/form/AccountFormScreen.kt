package com.duduapps.mybanks.features.account.form

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duduapps.mybanks.R
import com.duduapps.mybanks.ui.components.AppTopBar
import com.duduapps.mybanks.ui.components.LoadingDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormScreen(
    accountId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AccountFormViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var bankFilterText by remember { mutableStateOf("") }

    LaunchedEffect(accountId) {
        viewModel.initialize(accountId ?: 0L)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountFormEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AccountFormEvent.AccountSaved -> onNavigateBack()
                is AccountFormEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val title = if (uiState.isEditMode) {
        stringResource(R.string.label_edit_account, uiState.label)
    } else {
        stringResource(R.string.action_add_account)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::onLabelChanged,
                label = { Text(stringResource(R.string.account_nickname)) },
                placeholder = { Text("Ex: Nubank, Banco do Brasil") },
                isError = uiState.labelError != null,
                supportingText = uiState.labelError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bank Selection
            ExposedDropdownMenuBox(
                expanded = bankDropdownExpanded,
                onExpandedChange = { bankDropdownExpanded = it },
            ) {
                val bankDisplayText = uiState.selectedBank?.getDisplayName() ?: bankFilterText
                OutlinedTextField(
                    value = if (bankDropdownExpanded) bankFilterText else bankDisplayText,
                    onValueChange = {
                        bankFilterText = it
                        bankDropdownExpanded = true
                    },
                    label = { Text(stringResource(R.string.bank)) },
                    placeholder = { Text(stringResource(R.string.search)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                    isError = uiState.bankError != null,
                    supportingText = uiState.bankError?.let { { Text(it) } },
                    singleLine = true,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                )

                val filteredBanks = if (bankFilterText.isEmpty()) {
                    uiState.banks
                } else {
                    uiState.banks.filter {
                        it.name.contains(bankFilterText, ignoreCase = true) ||
                            it.code.contains(bankFilterText, ignoreCase = true)
                    }
                }

                if (filteredBanks.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                    ) {
                        filteredBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank.getDisplayName()) },
                                onClick = {
                                    viewModel.onBankSelected(bank)
                                    bankFilterText = ""
                                    bankDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.pixCode,
                onValueChange = viewModel::onPixCodeChanged,
                label = { Text(stringResource(R.string.pix_code)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.agency,
                    onValueChange = viewModel::onAgencyChanged,
                    label = { Text(stringResource(R.string.agency)) },
                    isError = uiState.agencyError != null,
                    supportingText = uiState.agencyError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = uiState.account,
                    onValueChange = viewModel::onAccountChanged,
                    label = { Text(stringResource(R.string.account)) },
                    isError = uiState.accountError != null,
                    supportingText = uiState.accountError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(1.5f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.operation,
                onValueChange = viewModel::onOperationChanged,
                label = { Text(stringResource(R.string.operation)) },
                placeholder = { Text("Ex: 013 (Poupança CEF)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Type
            Text(
                text = stringResource(R.string.type),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val checkingText = stringResource(R.string.checking)
            val savingsText = stringResource(R.string.savings)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = (uiState.type == checkingText),
                            onClick = { viewModel.onTypeChanged(checkingText) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (uiState.type == checkingText),
                        onClick = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = checkingText)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = (uiState.type == savingsText),
                            onClick = { viewModel.onTypeChanged(savingsText) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (uiState.type == savingsText),
                        onClick = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = savingsText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.holder,
                onValueChange = viewModel::onHolderChanged,
                label = { Text(stringResource(R.string.holder)) },
                isError = uiState.holderError != null,
                supportingText = uiState.holderError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLegalAccountChanged(!uiState.isLegalAccount) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = uiState.isLegalAccount,
                    onCheckedChange = viewModel::onLegalAccountChanged,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.is_legal_account))
            }

            Spacer(modifier = Modifier.height(4.dp))

            val docLabel = if (uiState.isLegalAccount) {
                stringResource(R.string.cnpj)
            } else {
                stringResource(R.string.cpf)
            }

            OutlinedTextField(
                value = uiState.document,
                onValueChange = viewModel::onDocumentChanged,
                label = { Text(docLabel) },
                isError = uiState.documentError != null,
                supportingText = uiState.documentError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            val submitText = if (uiState.isEditMode) {
                stringResource(R.string.save_changes)
            } else {
                stringResource(R.string.save_account)
            }

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = submitText)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (uiState.isLoading) {
        LoadingDialog(message = "Salvando conta...")
    }

    if (uiState.isSuccessDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissSuccessDialog()
                onNavigateBack()
            },
            title = { Text(stringResource(R.string.success)) },
            text = { Text(stringResource(R.string.success_account_added)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissSuccessDialog()
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.finish))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onResetFormForNewAccount) {
                    Text(stringResource(R.string.register_new))
                }
            },
        )
    }
}
