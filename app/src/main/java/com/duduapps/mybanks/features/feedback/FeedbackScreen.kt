package com.duduapps.mybanks.features.feedback

import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedbackEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is FeedbackEvent.ShowSuccessDialog -> {
                    successMessage = event.message
                }
                is FeedbackEvent.ShowErrorDialog -> {
                    errorMessage = event.message
                }
                is FeedbackEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    FeedbackScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onCommentsChanged = viewModel::onCommentsChanged,
        onSubmit = viewModel::onSubmit,
        successMessage = successMessage,
        errorMessage = errorMessage,
        onDismissSuccessDialog = {
            successMessage = null
            onNavigateBack()
        },
        onDismissErrorDialog = {
            errorMessage = null
        },
    )
}

@Composable
internal fun FeedbackScreenContent(
    uiState: FeedbackUiState,
    onNavigateBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onCommentsChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    successMessage: String? = null,
    errorMessage: String? = null,
    onDismissSuccessDialog: () -> Unit = {},
    onDismissErrorDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.send_feedback),
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
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(R.string.name)) },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text(stringResource(R.string.email)) },
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.comments,
                onValueChange = onCommentsChanged,
                label = { Text(stringResource(R.string.message)) },
                isError = uiState.commentsError != null,
                supportingText = uiState.commentsError?.let { { Text(it) } },
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.send_feedback))
            }
        }
    }

    if (uiState.isLoading) {
        LoadingDialog(message = stringResource(R.string.sending_feedback))
    }

    successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissSuccessDialog,
            title = { Text(stringResource(R.string.success)) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissSuccessDialog) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissErrorDialog,
            title = { Text(stringResource(R.string.ops)) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissErrorDialog) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}

@Preview(name = "Feedback - Empty", showBackground = true)
@Composable
private fun FeedbackEmptyPreview() {
    MyBanksTheme {
        FeedbackScreenContent(
            uiState = FeedbackPreviewsData.emptyState,
            onNavigateBack = {},
            onNameChanged = {},
            onEmailChanged = {},
            onCommentsChanged = {},
            onSubmit = {},
        )
    }
}

@Preview(name = "Feedback - Filled", showBackground = true)
@Composable
private fun FeedbackFilledPreview() {
    MyBanksTheme {
        FeedbackScreenContent(
            uiState = FeedbackPreviewsData.filledState,
            onNavigateBack = {},
            onNameChanged = {},
            onEmailChanged = {},
            onCommentsChanged = {},
            onSubmit = {},
        )
    }
}

@Preview(name = "Feedback - Errors", showBackground = true)
@Composable
private fun FeedbackErrorsPreview() {
    MyBanksTheme {
        FeedbackScreenContent(
            uiState = FeedbackPreviewsData.errorState,
            onNavigateBack = {},
            onNameChanged = {},
            onEmailChanged = {},
            onCommentsChanged = {},
            onSubmit = {},
        )
    }
}
