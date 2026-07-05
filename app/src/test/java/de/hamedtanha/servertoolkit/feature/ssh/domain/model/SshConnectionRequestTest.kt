package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SshConnectionRequestTest {

    @Test
    fun `stores connection target data and defaults to no authentication input`() {
        val request = SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )

        assertEquals("server-1", request.serverId)
        assertEquals("example.com", request.host)
        assertEquals(22, request.port)
        assertEquals("admin", request.username)
        assertEquals(SshAuthenticationInput.None, request.authenticationInput)
    }

    @Test
    fun `redacts authentication secret from string representation`() {
        val request = SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
            authenticationInput = SshAuthenticationInput.Password("secret-password"),
        )

        assertFalse(request.toString().contains("secret-password"))
    }

    @Test
    fun `clears authentication input sensitive values`() {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val request = SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
            authenticationInput = authenticationInput,
        )

        request.clearAuthenticationInput()

        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }
}
