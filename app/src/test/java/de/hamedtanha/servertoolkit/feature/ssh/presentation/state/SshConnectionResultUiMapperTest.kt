package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import org.junit.Assert.assertEquals
import org.junit.Test
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult

class SshConnectionResultUiMapperTest {

    @Test
    fun `maps connected result to connected ui state`() {
        val uiState = SshUiState(serverId = "server-1")
            .withConnectionResult(sshConnectedResult())

        assertEquals(SshConnectionStatus.Connected, uiState.status)
        assertEquals("Connected", uiState.statusLabel)
        assertEquals("SSH connection is ready.", uiState.message)
        assertEquals(
            "A project-owned SSH session handle was opened.",
            uiState.detail,
        )
        assertEquals("server-1", uiState.serverId)
    }

    @Test
    fun `maps timeout result to failed ui state`() {
        val uiState = SshUiState(serverId = "server-1")
            .withConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout),
            )

        assertEquals(SshConnectionStatus.Failed, uiState.status)
        assertEquals("Connection timed out", uiState.statusLabel)
        assertEquals("The connection attempt timed out.", uiState.message)
        assertEquals("The SSH session was not opened before the connection timeout elapsed.", uiState.detail)
        assertEquals("server-1", uiState.serverId)
    }

    @Test
    fun `maps host trust result to failed ui state`() {
        val uiState = SshUiState(serverId = "server-1")
            .withConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.HostTrustRequired),
            )

        assertEquals(SshConnectionStatus.Failed, uiState.status)
        assertEquals("Server identity review required", uiState.statusLabel)
        assertEquals("Review and trust the server identity before connecting.", uiState.message)
        assertEquals(
            "No SSH session was opened because the server identity must be reviewed first.",
            uiState.detail,
        )
    }

    @Test
    fun `maps all connection failure states to user-facing copy`() {
        val cases = listOf(
            ExpectedConnectionFailure(
                error = SshConnectionError.TargetNotFound,
                statusLabel = "Server unavailable",
                message = "The selected server could not be found.",
                detail = "The server inventory entry may have been deleted or is no longer available.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.MissingConnectionMetadata,
                statusLabel = "Connection details missing",
                message = "The selected server is missing required SSH connection metadata.",
                detail = "Check that the server has a host, port, and SSH username before connecting.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.UnknownHost,
                statusLabel = "Host unavailable",
                message = "The server host could not be resolved.",
                detail = "Check the host name or network connectivity, then try again.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.ConnectionTimeout,
                statusLabel = "Connection timed out",
                message = "The connection attempt timed out.",
                detail = "The SSH session was not opened before the connection timeout elapsed.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.AuthenticationRequired,
                statusLabel = "Authentication required",
                message = "Enter valid SSH authentication details before connecting.",
                detail = "No SSH session was opened because authentication did not complete successfully.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.HostTrustRequired,
                statusLabel = "Server identity review required",
                message = "Review and trust the server identity before connecting.",
                detail = "No SSH session was opened because the server identity must be reviewed first.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.UnsupportedConfiguration,
                statusLabel = "Unsupported SSH configuration",
                message = "This SSH configuration is not supported yet.",
                detail = "Use a supported password-based SSH configuration for this connection attempt.",
            ),
            ExpectedConnectionFailure(
                error = SshConnectionError.Unknown,
                statusLabel = "Connection failed",
                message = "The connection attempt failed.",
                detail = "No SSH session was opened.",
            ),
        )

        cases.forEach { expected ->
            val uiState = SshUiState(serverId = "server-1")
                .withConnectionResult(SshConnectionResult.Failed(expected.error))

            assertEquals(SshConnectionStatus.Failed, uiState.status)
            assertEquals(expected.statusLabel, uiState.statusLabel)
            assertEquals(expected.message, uiState.message)
            assertEquals(expected.detail, uiState.detail)
            assertEquals("server-1", uiState.serverId)
        }
    }

    private data class ExpectedConnectionFailure(
        val error: SshConnectionError,
        val statusLabel: String,
        val message: String,
        val detail: String,
    )

}
