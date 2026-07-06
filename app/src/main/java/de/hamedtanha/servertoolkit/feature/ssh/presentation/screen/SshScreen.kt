package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshHostKeyReviewUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel.SshViewModel

@Composable
fun SshRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SshViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SshScreen(
        uiState = uiState,
        onConnectClick = viewModel::onConnectClicked,
        onConfirmHostKeyClick = viewModel::onConfirmHostKeyClicked,
        onCancelHostKeyReviewClick = viewModel::onCancelHostKeyReviewClicked,
        onPasswordChange = viewModel::onPasswordChanged,
        onCommandChange = viewModel::onCommandChanged,
        onExecuteCommandClick = viewModel::onExecuteCommandClicked,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun SshScreen(
    uiState: SshUiState,
    onConnectClick: () -> Unit,
    onConfirmHostKeyClick: () -> Unit,
    onCancelHostKeyReviewClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onCommandChange: (String) -> Unit,
    onExecuteCommandClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Server id: ${uiState.serverId}",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Status: ${uiState.statusLabel}",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = uiState.message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.detail,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        uiState.hostKeyReview?.let { review ->
            Spacer(modifier = Modifier.height(16.dp))
            HostKeyReviewContent(review)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isHostKeyReviewRequired) {
            Button(
                onClick = onConfirmHostKeyClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Trust server identity")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCancelHostKeyReviewClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Cancel identity review")
            }
        } else {
            OutlinedTextField(
                value = password,
                onValueChange = { value ->
                    password = value
                    onPasswordChange(value)
                },
                label = {
                    Text(text = "Password")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                enabled = uiState.canStartConnection,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onConnectClick()
                    password = ""
                },
                enabled = uiState.canStartConnection,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Connect")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CommandExecutionContent(
            commandExecution = uiState.commandExecution,
            canExecuteCommand = uiState.canExecuteCommand,
            isConnected = uiState.status == SshConnectionStatus.Connected,
            onCommandChange = onCommandChange,
            onExecuteCommandClick = onExecuteCommandClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back")
        }
    }
}

@Composable
private fun CommandExecutionContent(
    commandExecution: SshCommandExecutionUiState,
    canExecuteCommand: Boolean,
    isConnected: Boolean,
    onCommandChange: (String) -> Unit,
    onExecuteCommandClick: () -> Unit,
) {
    Text(
        text = "Command execution",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = commandExecution.message,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = commandExecution.command,
        onValueChange = onCommandChange,
        label = {
            Text(text = "Command")
        },
        placeholder = {
            Text(text = "uptime")
        },
        enabled = isConnected && commandExecution.status != SshCommandExecutionStatus.Running,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onExecuteCommandClick,
        enabled = canExecuteCommand,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Run command")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Command status: ${commandExecution.statusLabel}",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (commandExecution.hasOutput) {
        Spacer(modifier = Modifier.height(12.dp))

        CommandOutputContent(commandExecution)
    }
}

@Composable
private fun CommandOutputContent(
    commandExecution: SshCommandExecutionUiState,
) {
    commandExecution.exitStatus?.let { exitStatus ->
        Text(
            text = "Exit status: $exitStatus",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (commandExecution.stdout.isNotBlank()) {
        Text(
            text = "stdout",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = commandExecution.stdout,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (commandExecution.stderr.isNotBlank()) {
        Text(
            text = "stderr",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = commandExecution.stderr,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HostKeyReviewContent(
    review: SshHostKeyReviewUiState,
) {
    Text(
        text = "Host: ${review.host}:${review.port}",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Fingerprint: ${review.displayFingerprint}",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
