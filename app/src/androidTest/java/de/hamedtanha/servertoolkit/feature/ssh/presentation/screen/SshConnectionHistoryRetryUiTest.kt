package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionHistoryItemUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionHistoryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SshConnectionHistoryRetryUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun blockingHistoryFailureRetryInvokesCallback() {
        var retryCallCount = 0

        setScreen(
            uiState = SshConnectionHistoryUiState(
                serverId = "server-1",
                errorMessage = "SSH connection history could not be loaded.",
            ),
            onRetryLoad = {
                retryCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, retryCallCount)
    }

    @Test
    fun laterHistoryFailureKeepsEntryVisibleAndRetryInvokesCallback() {
        var retryCallCount = 0

        setScreen(
            uiState = SshConnectionHistoryUiState(
                serverId = "server-1",
                entries = listOf(historyEntry()),
                errorMessage = "SSH connection history could not be loaded.",
            ),
            onRetryLoad = {
                retryCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithText("Connected")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("SSH connection history could not be loaded.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, retryCallCount)
    }

    private fun setScreen(
        uiState: SshConnectionHistoryUiState,
        onRetryLoad: () -> Unit,
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                SshConnectionHistoryScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetryLoad = onRetryLoad,
                )
            }
        }
    }

    private fun historyEntry(): SshConnectionHistoryItemUiState {
        return SshConnectionHistoryItemUiState(
            id = "history-1",
            endpointLabel = "example.com:22",
            username = "admin",
            status = SshConnectionHistoryStatus.Connected,
            statusLabel = "Connected",
            attemptedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            errorLabel = null,
        )
    }
}
