package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshUiStateSavedCommandSelectorTest {

    @Test
    fun `saved command selector is hidden and unavailable initially`() {
        val state = SshUiState()

        assertFalse(state.savedCommandSelector.isVisible)
        assertFalse(state.canEditCommandInput)
        assertFalse(state.canOpenSavedCommandSelector)
    }

    @Test
    fun `connected idle command input can open saved command selector`() {
        val state = SshUiState(
            status = SshConnectionStatus.Connected,
        )

        assertTrue(state.canEditCommandInput)
        assertTrue(state.canOpenSavedCommandSelector)
    }

    @Test
    fun `visible selector prevents duplicate open action`() {
        val state = SshUiState(
            status = SshConnectionStatus.Connected,
            savedCommandSelector = SshSavedCommandSelectorUiState.Loading,
        )

        assertTrue(state.canEditCommandInput)
        assertFalse(state.canOpenSavedCommandSelector)
    }

    @Test
    fun `running command blocks input editing and selector opening`() {
        val state = SshUiState(
            status = SshConnectionStatus.Connected,
            commandExecution = SshCommandExecutionUiState(
                command = "uptime",
                status = SshCommandExecutionStatus.Running,
                statusLabel = "Running",
            ),
        )

        assertFalse(state.canEditCommandInput)
        assertFalse(state.canOpenSavedCommandSelector)
    }
}
