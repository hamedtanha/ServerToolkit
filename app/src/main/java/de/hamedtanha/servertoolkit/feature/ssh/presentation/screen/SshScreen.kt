package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
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
            Button(
                onClick = onConnectClick,
                enabled = uiState.status != SshConnectionStatus.Connecting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Connect")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back")
        }
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
