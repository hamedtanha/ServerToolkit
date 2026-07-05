package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `private key passphrase input exposes method and redacts secret from string representation`() {
        val input = SshAuthenticationInput.PrivateKeyPassphrase("secret-passphrase")

        assertEquals(SshAuthenticationMethod.PRIVATE_KEY, input.method)
        assertTrue(input.hasSensitiveValue)
        assertEquals("secret-passphrase", input.passphrase)
        assertFalse(input.toString().contains("secret-passphrase"))
    }

    @Test
    fun `private key passphrase input clears sensitive value`() {
        val input = SshAuthenticationInput.PrivateKeyPassphrase("secret-passphrase")

        input.clearSensitiveValues()

        assertEquals("", input.passphrase)
        assertFalse(input.hasSensitiveValue)
    }

    @Test
    fun `none input does not contain sensitive value`() {
        val input = SshAuthenticationInput.None

        assertEquals(null, input.method)
        assertFalse(input.hasSensitiveValue)
    }
}
