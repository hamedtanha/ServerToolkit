package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SshConnectionHistoryEntityMapperTest {

    @Test
    fun `maps connection history domain model to entity`() {
        val domain = connectionHistoryEntry(
            status = SshConnectionHistoryStatus.Failed,
            connectionError = SshConnectionError.ConnectionTimeout,
        )

        val entity = domain.toEntity()

        assertEquals("history-1", entity.id)
        assertEquals("server-1", entity.serverId)
        assertEquals("example.com", entity.host)
        assertEquals(22, entity.port)
        assertEquals("admin", entity.username)
        assertEquals("Failed", entity.status)
        assertEquals(1_000L, entity.attemptedAtEpochMillis)
        assertEquals(2_000L, entity.completedAtEpochMillis)
        assertEquals("ConnectionTimeout", entity.connectionError)
    }

    @Test
    fun `maps connection history entity to domain model`() {
        val entity = connectionHistoryEntity(
            status = "Failed",
            connectionError = "ConnectionTimeout",
        )

        val domain = entity.toDomain()

        assertEquals("history-1", domain.id)
        assertEquals("server-1", domain.serverId)
        assertEquals("example.com", domain.host)
        assertEquals(22, domain.port)
        assertEquals("admin", domain.username)
        assertEquals(SshConnectionHistoryStatus.Failed, domain.status)
        assertEquals(1_000L, domain.attemptedAtEpochMillis)
        assertEquals(2_000L, domain.completedAtEpochMillis)
        assertEquals(SshConnectionError.ConnectionTimeout, domain.connectionError)
    }

    @Test
    fun `maps nullable completion and error fields`() {
        val domain = connectionHistoryEntry(
            status = SshConnectionHistoryStatus.Cancelled,
            completedAtEpochMillis = null,
            connectionError = null,
        )

        val entity = domain.toEntity()
        val mappedDomain = entity.toDomain()

        assertEquals(null, entity.completedAtEpochMillis)
        assertEquals(null, entity.connectionError)
        assertEquals(domain, mappedDomain)
    }

    private fun connectionHistoryEntry(
        status: SshConnectionHistoryStatus = SshConnectionHistoryStatus.Connected,
        completedAtEpochMillis: Long? = 2_000,
        connectionError: SshConnectionError? = null,
    ): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = "history-1",
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
            status = status,
            attemptedAtEpochMillis = 1_000,
            completedAtEpochMillis = completedAtEpochMillis,
            connectionError = connectionError,
        )
    }

    private fun connectionHistoryEntity(
        status: String = "Connected",
        completedAtEpochMillis: Long? = 2_000,
        connectionError: String? = null,
    ): SshConnectionHistoryEntity {
        return SshConnectionHistoryEntity(
            id = "history-1",
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
            status = status,
            attemptedAtEpochMillis = 1_000,
            completedAtEpochMillis = completedAtEpochMillis,
            connectionError = connectionError,
        )
    }
}
