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
}
