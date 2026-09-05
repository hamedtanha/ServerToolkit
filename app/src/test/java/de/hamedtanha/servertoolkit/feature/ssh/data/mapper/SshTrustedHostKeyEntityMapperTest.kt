package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SshTrustedHostKeyEntityMapperTest {

    @Test
    fun `maps canonical OpenSSH sha256 fingerprint to distinct persisted scheme`() {
        val trustedHostKey = trustedHostKey()

        val entity = trustedHostKey.toEntity()

        assertEquals("server-1", entity.serverId)
        assertEquals("example.com", entity.host)
        assertEquals(22, entity.port)
        assertEquals(PERSISTED_OPENSSH_SHA256_V1, entity.fingerprintAlgorithm)
        assertEquals("abc123", entity.fingerprintValue)
        assertEquals(trustedHostKey, entity.toDomain())
    }

    @Test
    fun `round trips historical Java encoded sha256 row without relabeling`() {
        val entity = trustedHostKeyEntity(
            fingerprintAlgorithm = "SHA256",
            fingerprintValue = "legacy-java-sha256",
        )

        val domain = entity.toDomain()

        assertEquals("SHA256", domain.fingerprint.algorithm)
        assertEquals(
            SshHostKeyFingerprintEncoding.LegacyJavaPublicKey,
            domain.fingerprint.encoding,
        )
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `round trips historical SSHJ md5 row without relabeling`() {
        val entity = trustedHostKeyEntity(
            fingerprintAlgorithm = "MD5",
            fingerprintValue = "aa:bb:cc:dd",
        )

        val domain = entity.toDomain()

        assertEquals("MD5", domain.fingerprint.algorithm)
        assertEquals(
            SshHostKeyFingerprintEncoding.OpenSshWire,
            domain.fingerprint.encoding,
        )
        assertEquals(entity, domain.toEntity())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unknown persisted fingerprint scheme`() {
        trustedHostKeyEntity(
            fingerprintAlgorithm = "UNKNOWN_FINGERPRINT_SCHEME",
            fingerprintValue = "value",
        ).toDomain()
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
                encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
            ),
        )
    }

    private fun trustedHostKeyEntity(
        fingerprintAlgorithm: String,
        fingerprintValue: String,
    ): SshTrustedHostKeyEntity {
        return SshTrustedHostKeyEntity(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            fingerprintAlgorithm = fingerprintAlgorithm,
            fingerprintValue = fingerprintValue,
        )
    }
}
