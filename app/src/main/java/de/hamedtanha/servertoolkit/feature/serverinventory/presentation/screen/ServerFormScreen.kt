package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerFormUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel.AddServerViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel.EditServerViewModel
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitButtonShape

@Composable
fun AddServerRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddServerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerFormRouteContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onHostChanged = viewModel::onHostChanged,
        onPortChanged = viewModel::onPortChanged,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun EditServerRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditServerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerFormRouteContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onHostChanged = viewModel::onHostChanged,
        onPortChanged = viewModel::onPortChanged,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onRetryLoad = viewModel::onRetryLoad,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ServerFormRouteContent(
    uiState: ServerFormUiState,
    onNameChanged: (String) -> Unit,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    onRetryLoad: (() -> Unit)? = null,
) {
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    ServerFormScreen(
        uiState = uiState,
        onNameChanged = onNameChanged,
        onHostChanged = onHostChanged,
        onPortChanged = onPortChanged,
        onUsernameChanged = onUsernameChanged,
        onSaveClicked = onSaveClicked,
        onNavigateBack = onNavigateBack,
        onRetryLoad = onRetryLoad,
    )
}

@Composable
fun ServerFormScreen(
    uiState: ServerFormUiState,
    onNameChanged: (String) -> Unit,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryLoad: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Server name") },
            singleLine = true,
            isError = uiState.nameError != null,
            supportingText = {
                uiState.nameError?.let { errorMessage ->
                    Text(text = errorMessage)
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.host,
            onValueChange = onHostChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Host or IP address") },
            singleLine = true,
            isError = uiState.hostError != null,
            supportingText = {
                uiState.hostError?.let { errorMessage ->
                    Text(text = errorMessage)
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.port,
            onValueChange = onPortChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "SSH port") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.portError != null,
            supportingText = {
                uiState.portError?.let { errorMessage ->
                    Text(text = errorMessage)
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Username") },
            singleLine = true,
            isError = uiState.usernameError != null,
            supportingText = {
                uiState.usernameError?.let { errorMessage ->
                    Text(text = errorMessage)
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.formMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.canRetryLoad && onRetryLoad != null) {
            Button(
                shape = ServerToolkitButtonShape,
                onClick = onRetryLoad,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Retry loading server")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            shape = ServerToolkitButtonShape,
            onClick = onSaveClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSave,
        ) {
            Text(
                text = if (uiState.isSaving) {
                    "Saving..."
                } else {
                    "Save server"
                },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            shape = ServerToolkitButtonShape,
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back to server inventory")
        }
    }
}
