package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import de.hamedtanha.servertoolkit.feature.ssh.test.TrackingSshPrivateKeySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SshAuthenticationInputTest {

    @Test
    fun `password input exposes method and redacts secret from string representation`() {
        val input = SshAuthenticationInput.Password("secret-password")

        assertEquals(SshAuthenticationMethod.PASSWORD, input.method)
        assertTrue(input.hasSensitiveValue)
        assertEquals("secret-password", input.password)
        assertFalse(input.toString().contains("secret-password"))
    }

    @Test
    fun `password input clears sensitive value`() {
        val input = SshAuthenticationInput.Password("secret-password")

        input.clearSensitiveValues()

        assertEquals("", input.password)
        assertFalse(input.hasSensitiveValue)
    }

    @Test
    fun `private key input exposes only ownership and passphrase accessors`() {
        val source = TrackingSshPrivateKeySource()
        val input = SshAuthenticationInput.PrivateKey(
            privateKeySource = source,
            passphrase = "secret-passphrase",
        )

        assertEquals(SshAuthenticationMethod.PRIVATE_KEY, input.method)
        assertTrue(input.hasPrivateKeySource)
        assertTrue(input.hasSensitiveValue)
        assertEquals("secret-passphrase", input.passphrase)
        assertFalse(input.toString().contains("secret-passphrase"))
        assertFalse(input.toString().contains(source.toString()))
    }

    @Test
    fun `private key input transfers source exactly once`() {
        val source = TrackingSshPrivateKeySource()
        val input = SshAuthenticationInput.PrivateKey(
            privateKeySource = source,
            passphrase = "",
        )

        assertSame(source, input.takePrivateKeySource())
        assertNull(input.takePrivateKeySource())
        assertFalse(input.hasPrivateKeySource)
        assertEquals(0, source.invalidateCallCount)
    }

    @Test
    fun `private key input invalidates source and clears passphrase`() {
        val source = TrackingSshPrivateKeySource()
        val input = SshAuthenticationInput.PrivateKey(
            privateKeySource = source,
            passphrase = "secret-passphrase",
        )

        input.clearSensitiveValues()

        assertEquals("", input.passphrase)
        assertFalse(input.hasPrivateKeySource)
        assertFalse(input.hasSensitiveValue)
        assertEquals(1, source.invalidateCallCount)
    }

    @Test
    fun `none input does not contain sensitive value`() {
        val input = SshAuthenticationInput.None

        assertEquals(null, input.method)
        assertFalse(input.hasSensitiveValue)
    }
}
