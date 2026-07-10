package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SshConnectionHistoryUiMapperTest {

    @Test
    fun `maps connected history entry to ui state`() {
        val uiState = historyEntry(
            status = SshConnectionHistoryStatus.Connected,
        ).toUiState()

        assertEquals("history-1", uiState.id)
        assertEquals("example.com:2222", uiState.endpointLabel)
        assertEquals("admin", uiState.username)
        assertEquals(SshConnectionHistoryStatus.Connected, uiState.status)
        assertEquals("Connected", uiState.statusLabel)
        assertEquals(1_000L, uiState.attemptedAtEpochMillis)
        assertEquals(2_500L, uiState.completedAtEpochMillis)
        assertEquals(1_500L, uiState.durationMillis)
        assertEquals(null, uiState.errorLabel)
    }

    @Test
    fun `maps failed history entry error to user facing label`() {
        val uiState = historyEntry(
            status = SshConnectionHistoryStatus.Failed,
            connectionError = SshConnectionError.ConnectionTimeout,
        ).toUiState()

        assertEquals("Failed", uiState.statusLabel)
        assertEquals("Connection timed out", uiState.errorLabel)
    }

    @Test
    fun `maps cancelled history entry without failure detail`() {
        val uiState = historyEntry(
            status = SshConnectionHistoryStatus.Cancelled,
            completedAtEpochMillis = null,
        ).toUiState()

        assertEquals("Cancelled", uiState.statusLabel)
        assertEquals(null, uiState.durationMillis)
        assertEquals(null, uiState.errorLabel)
    }

    private fun historyEntry(
        status: SshConnectionHistoryStatus,
        completedAtEpochMillis: Long? = 2_500L,
        connectionError: SshConnectionError? = null,
    ): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = "history-1",
            serverId = "server-1",
            host = "example.com",
            port = 2222,
            username = "admin",
            status = status,
            attemptedAtEpochMillis = 1_000L,
            completedAtEpochMillis = completedAtEpochMillis,
            connectionError = connectionError,
        )
    }
}
