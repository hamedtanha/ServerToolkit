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
        assertEquals("Command failed", mapped.statusLabel)
        assertEquals("No active SSH session was found.", mapped.message)
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
}
