package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshPrivateKeyMaterialTest {

    @Test
    fun `exposes valid bytes internally and redacts string representation`() {
        val secret = "secret-private-key"
        val bytes = secret.toByteArray(StandardCharsets.UTF_8)
        val material = SshPrivateKeyMaterial(
            bytes = bytes,
            validSize = bytes.size,
        )

        val observed = material.useBytes { source, size ->
            String(source, 0, size, StandardCharsets.UTF_8)
        }

        assertEquals(secret, observed)
        assertEquals(bytes.size, material.size)
        assertFalse(material.isCleared)
        assertFalse(material.toString().contains(secret))
    }

    @Test
    fun `clear zeroes backing bytes and prevents further access`() {
        val bytes = "secret-private-key".toByteArray(StandardCharsets.UTF_8)
        val material = SshPrivateKeyMaterial(
            bytes = bytes,
            validSize = bytes.size,
        )

        material.clear()

        assertTrue(material.isCleared)
        assertEquals(0, material.size)
        assertTrue(bytes.all { it == 0.toByte() })

        try {
            material.useBytes { _, _ -> Unit }
            fail("Expected cleared material access to fail.")
        } catch (_: IllegalStateException) {
            // Expected.
        }
    }

    @Test
    fun `clear is idempotent`() {
        val bytes = "secret-private-key".toByteArray(StandardCharsets.UTF_8)
        val material = SshPrivateKeyMaterial(
            bytes = bytes,
            validSize = bytes.size,
        )

        material.clear()
        material.clear()

        assertTrue(material.isCleared)
        assertEquals(0, material.size)
        assertTrue(bytes.all { it == 0.toByte() })
    }
}
