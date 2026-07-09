package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshCommandExecutionUiMapperTest {

    @Test
    fun `maps command text and clears previous output`() {
        val state = SshCommandExecutionUiState(
            stdout = "old output",
            stderr = "old error",
            exitStatus = 1,
        )

        val mapped = state.withCommandText("uptime")

        assertEquals("uptime", mapped.command)
        assertEquals(SshCommandExecutionStatus.Idle, mapped.status)
        assertEquals("", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(null, mapped.exitStatus)
        assertTrue(mapped.canExecute)
        assertFalse(mapped.hasOutput)
    }

    @Test
    fun `maps blank command text into non executable idle state`() {
        val state = SshCommandExecutionUiState(
            stdout = "old output",
            stderr = "old error",
            exitStatus = 1,
        )

        val mapped = state.withCommandText("   ")

        assertEquals("   ", mapped.command)
        assertEquals(SshCommandExecutionStatus.Idle, mapped.status)
        assertEquals("No command entered", mapped.statusLabel)
        assertEquals("Enter a command before running it.", mapped.message)
        assertEquals("Command execution requires a non-blank command.", mapped.detail)
        assertEquals("", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(null, mapped.exitStatus)
        assertFalse(mapped.canExecute)
        assertFalse(mapped.hasOutput)
    }

    @Test
    fun `maps running state and clears previous output`() {
        val state = SshCommandExecutionUiState(
            command = "uptime",
            stdout = "old output",
            stderr = "old error",
            exitStatus = 1,
        )

        val mapped = state.asRunning()

        assertEquals(SshCommandExecutionStatus.Running, mapped.status)
        assertEquals("Running command", mapped.statusLabel)
        assertEquals("", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(null, mapped.exitStatus)
        assertFalse(mapped.canExecute)
        assertFalse(mapped.hasOutput)
    }

    @Test
    fun `maps session unavailable state and clears previous output`() {
        val state = SshCommandExecutionUiState(
            command = "uptime",
            status = SshCommandExecutionStatus.Completed,
            stdout = "old output",
            stderr = "old error",
            exitStatus = 1,
        )

        val mapped = state.asSessionUnavailable()

        assertEquals("uptime", mapped.command)
        assertEquals(SshCommandExecutionStatus.Idle, mapped.status)
        assertEquals("No command executed", mapped.statusLabel)
        assertEquals("", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(null, mapped.exitStatus)
        assertFalse(mapped.hasOutput)
    }

    @Test
    fun `maps completed command result`() {
        val state = SshCommandExecutionUiState(command = "uptime")

        val mapped = state.withExecutionResult(
            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "server up",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        )

        assertEquals(SshCommandExecutionStatus.Completed, mapped.status)
        assertEquals("Command completed", mapped.statusLabel)
        assertEquals("server up", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(0, mapped.exitStatus)
        assertTrue(mapped.canExecute)
        assertTrue(mapped.hasOutput)
    }

    @Test
    fun `maps failed command result`() {
        val state = SshCommandExecutionUiState(
            command = "uptime",
            stdout = "old output",
            stderr = "old error",
            exitStatus = 1,
        )

        val mapped = state.withExecutionResult(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.SessionNotFound),
        )

        assertEquals(SshCommandExecutionStatus.Failed, mapped.status)
        assertEquals("No active SSH session", mapped.statusLabel)
        assertEquals("No active SSH session was found.", mapped.message)
        assertEquals("Connect to the server again before running a command.", mapped.detail)
        assertEquals("", mapped.stdout)
        assertEquals("", mapped.stderr)
        assertEquals(null, mapped.exitStatus)
        assertTrue(mapped.canExecute)
        assertFalse(mapped.hasOutput)
    }

    @Test
    fun `maps timeout command error message`() {
        val mapped = SshCommandExecutionUiState(command = "uptime").withExecutionResult(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut),
        )

        assertEquals("The command timed out.", mapped.message)
    }

    @Test
    fun `maps unknown command error message`() {
        val mapped = SshCommandExecutionUiState(command = "uptime").withExecutionResult(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.Unknown),
        )

        assertEquals("Command execution failed.", mapped.message)
    }

    @Test
    fun `maps all command failure states to user-facing copy`() {
        val cases = listOf(
            ExpectedCommandFailure(
                error = SshCommandExecutionError.SessionNotFound,
                statusLabel = "No active SSH session",
                message = "No active SSH session was found.",
                detail = "Connect to the server again before running a command.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.ChannelOpenFailed,
                statusLabel = "Command channel unavailable",
                message = "The SSH command channel could not be opened.",
                detail = "The active SSH session could not open a non-interactive command channel.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.CommandExecutionFailed,
                statusLabel = "Command execution failed",
                message = "The command could not be executed.",
                detail = "The command channel opened, but command execution did not complete successfully.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.CommandTimedOut,
                statusLabel = "Command timed out",
                message = "The command timed out.",
                detail = "The command did not complete before the configured timeout elapsed.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.CommandCancelled,
                statusLabel = "Command cancelled",
                message = "The command was cancelled.",
                detail = "The command was cancelled and the command channel was cleaned up.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.UnsupportedConfiguration,
                statusLabel = "Unsupported command configuration",
                message = "This command configuration is not supported.",
                detail = "Use a supported non-interactive command execution configuration.",
            ),
            ExpectedCommandFailure(
                error = SshCommandExecutionError.Unknown,
                statusLabel = "Command failed",
                message = "Command execution failed.",
                detail = "The command did not complete successfully.",
            ),
        )

        cases.forEach { expected ->
            val mapped = SshCommandExecutionUiState(command = "uptime")
                .withExecutionResult(SshCommandExecutionResult.Failed(expected.error))

            assertEquals(SshCommandExecutionStatus.Failed, mapped.status)
            assertEquals(expected.statusLabel, mapped.statusLabel)
            assertEquals(expected.message, mapped.message)
            assertEquals(expected.detail, mapped.detail)
            assertEquals("", mapped.stdout)
            assertEquals("", mapped.stderr)
            assertEquals(null, mapped.exitStatus)
            assertTrue(mapped.canExecute)
            assertFalse(mapped.hasOutput)
        }
    }

    private data class ExpectedCommandFailure(
        val error: SshCommandExecutionError,
        val statusLabel: String,
        val message: String,
        val detail: String,
    )

}
