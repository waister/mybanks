package com.duduapps.mybanks.features.auth

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duduapps.mybanks.R
import com.duduapps.mybanks.ui.components.AppTopBar
import com.duduapps.mybanks.ui.components.LoadingDialog
import com.duduapps.mybanks.ui.theme.MyBanksTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var dialogTitleRes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is LoginEvent.ShowSuccessDialog -> {
                    dialogTitleRes = R.string.success
                    dialogMessage = event.message
                }

                is LoginEvent.ShowErrorDialog -> {
                    dialogTitleRes = R.string.ops
                    dialogMessage = event.message
                }

                is LoginEvent.LoginSuccess -> currentOnLoginSuccess()

                is LoginEvent.NavigateBack -> currentOnNavigateBack()
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEmailChange = viewModel::onEmailChanged,
        onCodeChange = viewModel::onCodeChanged,
        onPositiveAction = viewModel::onPositiveAction,
        dialogTitleRes = dialogTitleRes,
        dialogMessage = dialogMessage,
        onDismissDialog = { dialogMessage = null },
    )
}

@Composable
internal fun LoginScreenContent(
    uiState: LoginUiState,
    onNavigateBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPositiveAction: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes dialogTitleRes: Int? = null,
    dialogMessage: String? = null,
    onDismissDialog: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.login_title),
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
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!uiState.isCodeStep) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.email)) },
                    placeholder = { Text(stringResource(R.string.write_your_email)) },
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = onCodeChange,
                    label = { Text(stringResource(R.string.code_sent_to_email)) },
                    placeholder = { Text("0000") },
                    isError = uiState.codeError != null,
                    supportingText = uiState.codeError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val positiveText = if (!uiState.isCodeStep) {
                stringResource(R.string.receive_code)
            } else {
                stringResource(R.string.confirm_code)
            }

            Button(
                onClick = onPositiveAction,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = positiveText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }

    if (uiState.isLoading) {
        LoadingDialog(message = stringResource(R.string.sending))
    }

    dialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = dialogTitleRes?.let { { Text(stringResource(it)) } },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}

@Preview(name = "Login - Email Step", showBackground = true)
@Composable
private fun LoginEmailStepPreview() {
    MyBanksTheme {
        LoginScreenContent(
            uiState = LoginPreviewsData.emailStepState,
            onNavigateBack = {},
            onEmailChange = {},
            onCodeChange = {},
            onPositiveAction = {},
        )
    }
}

@Preview(name = "Login - Code Step", showBackground = true)
@Composable
private fun LoginCodeStepPreview() {
    MyBanksTheme {
        LoginScreenContent(
            uiState = LoginPreviewsData.codeStepState,
            onNavigateBack = {},
            onEmailChange = {},
            onCodeChange = {},
            onPositiveAction = {},
        )
    }
}

@Preview(name = "Login - Errors", showBackground = true)
@Composable
private fun LoginErrorsPreview() {
    MyBanksTheme {
        LoginScreenContent(
            uiState = LoginPreviewsData.emailErrorState,
            onNavigateBack = {},
            onEmailChange = {},
            onCodeChange = {},
            onPositiveAction = {},
        )
    }
}
