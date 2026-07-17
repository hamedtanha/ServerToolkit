package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshSavedCommandSelectorUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SshScreenSavedCommandSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun connectedCommandInputCanOpenSavedCommandSelector() {
        var openCallCount = 0

        setScreen(
            uiState = connectedUiState(),
            onOpenSavedCommandSelector = {
                openCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithTag(
                SSH_OPEN_SAVED_COMMAND_SELECTOR_TEST_TAG,
            )
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertEquals(1, openCallCount)
    }

    @Test
    fun disconnectedCommandInputCannotOpenSavedCommandSelector() {
        setScreen(uiState = SshUiState())

        composeTestRule
            .onNodeWithTag(
                SSH_OPEN_SAVED_COMMAND_SELECTOR_TEST_TAG,
            )
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun loadingSelectorKeepsManualCommandInputEnabled() {
        var latestCommand = ""

        setScreen(
            uiState = connectedUiState(
                selector = SshSavedCommandSelectorUiState.Loading,
            ),
            onCommandChange = { command ->
                latestCommand = command
            },
        )

        composeTestRule
            .onNodeWithTag(SSH_COMMAND_INPUT_TEST_TAG)
            .performScrollTo()
            .assertIsEnabled()
            .performTextInput("manual while loading")

        assertEquals("manual while loading", latestCommand)

        composeTestRule
            .onNodeWithText("Loading saved commands.")
            .assertIsDisplayed()
    }

    @Test
    fun savedCommandSelectionDoesNotRunCommand() {
        val exactCommand = "  printf 'first\n  second'  "
        var selectedCommandId: String? = null
        var executionCallCount = 0

        setScreen(
            uiState = connectedUiState(
                selector = SshSavedCommandSelectorUiState.Content(
                    commands = listOf(
                        SavedCommand(
                            id = "command-1",
                            name = "System status",
                            command = exactCommand,
                            createdAtEpochMillis = 1_000L,
                        ),
                    ),
                ),
            ),
            onSavedCommandSelect = { commandId ->
                selectedCommandId = commandId
            },
            onExecuteCommandClick = {
                executionCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithTag(SSH_SAVED_COMMAND_SELECTOR_TEST_TAG)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("System status")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(exactCommand)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                "${SSH_SAVED_COMMAND_USE_TEST_TAG_PREFIX}command-1",
            )
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals("command-1", selectedCommandId)
        assertEquals(0, executionCallCount)
    }

    @Test
    fun selectorFailureExposesRetryAndCancelActions() {
        var retryCallCount = 0
        var cancelCallCount = 0

        setScreen(
            uiState = connectedUiState(
                selector = SshSavedCommandSelectorUiState.Failure(
                    errorMessage =
                        "Saved commands could not be loaded.",
                ),
            ),
            onRetrySavedCommandSelector = {
                retryCallCount += 1
            },
            onCancelSavedCommandSelector = {
                cancelCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithTag(SSH_SAVED_COMMAND_SELECTOR_TEST_TAG)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        assertEquals(1, retryCallCount)
        assertEquals(1, cancelCallCount)
    }

    private fun setScreen(
        uiState: SshUiState,
        onCommandChange: (String) -> Unit = {},
        onOpenSavedCommandSelector: () -> Unit = {},
        onRetrySavedCommandSelector: () -> Unit = {},
        onCancelSavedCommandSelector: () -> Unit = {},
        onSavedCommandSelect: (String) -> Unit = {},
        onExecuteCommandClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                SshScreen(
                    uiState = uiState,
                    onAuthenticationMethodSelect = {},
                    onPrivateKeySelectClick = {},
                    onConnectClick = {},
                    onDisconnectClick = {},
                    onConfirmHostKeyClick = {},
                    onCancelHostKeyReviewClick = {},
                    onPasswordChange = {},
                    onPrivateKeyPassphraseChange = {},
                    onCommandChange = onCommandChange,
                    onOpenSavedCommandSelector =
                        onOpenSavedCommandSelector,
                    onRetrySavedCommandSelector =
                        onRetrySavedCommandSelector,
                    onCancelSavedCommandSelector =
                        onCancelSavedCommandSelector,
                    onSavedCommandSelect =
                        onSavedCommandSelect,
                    onExecuteCommandClick =
                        onExecuteCommandClick,
                    onOpenConnectionHistory = {},
                    onNavigateBack = {},
                )
            }
        }
    }

    private fun connectedUiState(
        selector: SshSavedCommandSelectorUiState =
            SshSavedCommandSelectorUiState.Hidden,
    ): SshUiState {
        return SshUiState(
            status = SshConnectionStatus.Connected,
            savedCommandSelector = selector,
        )
    }
}
