package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitButtonShape
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionHistoryItemUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionHistoryUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel.SshConnectionHistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun SshConnectionHistoryRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SshConnectionHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SshConnectionHistoryScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun SshConnectionHistoryScreen(
    uiState: SshConnectionHistoryUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Connection history",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Server id: ${uiState.serverId}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> ConnectionHistoryLoadingContent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            uiState.errorMessage != null -> ConnectionHistoryMessageContent(
                title = "Connection history could not be loaded",
                message = uiState.errorMessage,
                isError = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            uiState.isEmpty -> ConnectionHistoryMessageContent(
                title = "No connection history",
                message = "No SSH connection attempts have been recorded for this server.",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            uiState.hasEntries -> LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.entries,
                    key = { entry -> entry.id },
                ) { entry ->
                    ConnectionHistoryItem(entry)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(

            shape = ServerToolkitButtonShape,
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back")
        }
    }
}

@Composable
private fun ConnectionHistoryLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ConnectionHistoryMessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectionHistoryItem(
    entry: SshConnectionHistoryItemUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.statusLabel,
                style = MaterialTheme.typography.titleMedium,
                color = when (entry.status) {
                    SshConnectionHistoryStatus.Connected -> MaterialTheme.colorScheme.primary
                    SshConnectionHistoryStatus.Failed -> MaterialTheme.colorScheme.error
                    SshConnectionHistoryStatus.Cancelled ->
                        MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Text(
                text = formatTimestamp(entry.attemptedAtEpochMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Endpoint: ${entry.endpointLabel}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "User: ${entry.username}",
                style = MaterialTheme.typography.bodyMedium,
            )

            entry.durationMillis?.let { durationMillis ->
                Text(
                    text = "Duration: ${formatDuration(durationMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            entry.errorLabel?.let { errorLabel ->
                Text(
                    text = errorLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
}

private fun formatDuration(durationMillis: Long): String {
    return if (durationMillis < MILLIS_PER_SECOND) {
        "$durationMillis ms"
    } else {
        String.format(
            Locale.getDefault(),
            "%.1f s",
            durationMillis / MILLIS_PER_SECOND.toDouble(),
        )
    }
}

private const val MILLIS_PER_SECOND = 1_000L
