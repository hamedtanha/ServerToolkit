package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjAuthenticationAdapterTest {

    private val adapter = SshjAuthenticationAdapter()

    @Test
    fun `maps missing authentication input into none mapping`() {
        val mapping = adapter.map(connectionRequest()) as SshjAuthenticationMapping.None

        assertEquals("admin", mapping.username)
        assertNull(mapping.method)
        assertFalse(mapping.hasSensitiveValue)

        mapping.clearSensitiveValues()

        assertFalse(mapping.hasSensitiveValue)
    }

    @Test
    fun `maps password authentication input without exposing secret in string representation`() {
        val input = SshAuthenticationInput.Password("secret-password")

        val mapping = adapter.map(
            connectionRequest(authenticationInput = input),
        ) as SshjAuthenticationMapping.Password

        assertEquals("admin", mapping.username)
        assertEquals(SshAuthenticationMethod.PASSWORD, mapping.method)
        assertEquals("secret-password", mapping.password)
        assertTrue(mapping.hasSensitiveValue)
        assertFalse(mapping.toString().contains("secret-password"))

        mapping.clearSensitiveValues()

        assertEquals("", mapping.password)
        assertFalse(mapping.hasSensitiveValue)
        assertFalse(input.hasSensitiveValue)
    }

    @Test
    fun `maps private key passphrase input without exposing secret in string representation`() {
        val input = SshAuthenticationInput.PrivateKeyPassphrase("secret-passphrase")

        val mapping = adapter.map(
            connectionRequest(authenticationInput = input),
        ) as SshjAuthenticationMapping.PrivateKeyPassphrase

        assertEquals("admin", mapping.username)
        assertEquals(SshAuthenticationMethod.PRIVATE_KEY, mapping.method)
        assertEquals("secret-passphrase", mapping.passphrase)
        assertTrue(mapping.hasSensitiveValue)
        assertFalse(mapping.toString().contains("secret-passphrase"))

        mapping.clearSensitiveValues()

        assertEquals("", mapping.passphrase)
        assertFalse(mapping.hasSensitiveValue)
        assertFalse(input.hasSensitiveValue)
    }

    private fun connectionRequest(
        authenticationInput: SshAuthenticationInput = SshAuthenticationInput.None,
    ): SshConnectionRequest {
        return SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
            authenticationInput = authenticationInput,
        )
    }
}
