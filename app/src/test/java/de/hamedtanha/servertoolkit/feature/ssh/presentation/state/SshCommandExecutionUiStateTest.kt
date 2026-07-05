package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshCommandExecutionUiStateTest {

    @Test
    fun `blank command cannot be executed`() {
        val state = SshCommandExecutionUiState(command = " ")

        assertFalse(state.canExecute)
    }

    @Test
    fun `non blank idle command can be executed`() {
        val state = SshCommandExecutionUiState(command = "uptime")

        assertTrue(state.canExecute)
    }

    @Test
    fun `running command cannot be executed again`() {
        val state = SshCommandExecutionUiState(
            command = "uptime",
            status = SshCommandExecutionStatus.Running,
        )

        assertFalse(state.canExecute)
    }

    @Test
    fun `state has output when stdout is present`() {
        val state = SshCommandExecutionUiState(stdout = "ok")

        assertTrue(state.hasOutput)
    }

    @Test
    fun `state has output when stderr is present`() {
        val state = SshCommandExecutionUiState(stderr = "error")

        assertTrue(state.hasOutput)
    }

    @Test
    fun `state has output when exit status is present`() {
        val state = SshCommandExecutionUiState(exitStatus = 0)

        assertTrue(state.hasOutput)
    }

    @Test
    fun `ssh ui state allows command execution only when connected and command is ready`() {
        val state = SshUiState(
            status = SshConnectionStatus.Connected,
            commandExecution = SshCommandExecutionUiState(command = "uptime"),
        )

        assertTrue(state.canExecuteCommand)
    }

    @Test
    fun `ssh ui state blocks command execution when not connected`() {
        val state = SshUiState(
            status = SshConnectionStatus.NotStarted,
            commandExecution = SshCommandExecutionUiState(command = "uptime"),
        )

        assertFalse(state.canExecuteCommand)
    }

    @Test
    fun `default command execution state is idle`() {
        val state = SshCommandExecutionUiState()

        assertEquals(SshCommandExecutionStatus.Idle, state.status)
        assertFalse(state.canExecute)
        assertFalse(state.hasOutput)
    }
}
