package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SshTrustedHostKeyEntityMapperTest {

    @Test
    fun `maps trusted host key domain model to entity`() {
        val trustedHostKey = trustedHostKey()

        val entity = trustedHostKey.toEntity()

        assertEquals("server-1", entity.serverId)
        assertEquals("example.com", entity.host)
        assertEquals(22, entity.port)
        assertEquals("SHA256", entity.fingerprintAlgorithm)
        assertEquals("abc123", entity.fingerprintValue)
    }

    @Test
    fun `maps trusted host key entity to domain model`() {
        val trustedHostKey = trustedHostKey()

        val entity = trustedHostKey.toEntity()
        val mappedDomain = entity.toDomain()

        assertEquals(trustedHostKey, mappedDomain)
    }

    private fun trustedHostKey(): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = SshHostEndpoint(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
            fingerprint = SshHostKeyFingerprint(
                algorithm = "SHA256",
                value = "abc123",
            ),
        )
    }
}
