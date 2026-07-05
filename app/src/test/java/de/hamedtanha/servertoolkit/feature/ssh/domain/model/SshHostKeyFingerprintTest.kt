package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshHostKeyFingerprintTest {

    @Test
    fun `creates fingerprint with algorithm and value`() {
        val fingerprint = SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = "abc123",
        )

        assertEquals("SHA256", fingerprint.algorithm)
        assertEquals("abc123", fingerprint.value)
    }

    @Test
    fun `rejects blank fingerprint algorithm`() {
        try {
            SshHostKeyFingerprint(
                algorithm = " ",
                value = "abc123",
            )
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Host key fingerprint algorithm must not be blank.", error.message)
        }
    }

    @Test
    fun `rejects blank fingerprint value`() {
        try {
            SshHostKeyFingerprint(
                algorithm = "SHA256",
                value = " ",
            )
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Host key fingerprint value must not be blank.", error.message)
        }
    }
}
