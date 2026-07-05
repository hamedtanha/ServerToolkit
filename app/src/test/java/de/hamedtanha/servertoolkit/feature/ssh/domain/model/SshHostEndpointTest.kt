package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshHostEndpointTest {

    @Test
    fun `creates endpoint with server id host and port`() {
        val endpoint = SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )

        assertEquals("server-1", endpoint.serverId)
        assertEquals("example.com", endpoint.host)
        assertEquals(22, endpoint.port)
    }

    @Test
    fun `rejects blank server id`() {
        try {
            SshHostEndpoint(
                serverId = " ",
                host = "example.com",
                port = 22,
            )
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Server id must not be blank.", error.message)
        }
    }

    @Test
    fun `rejects blank host`() {
        try {
            SshHostEndpoint(
                serverId = "server-1",
                host = " ",
                port = 22,
            )
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Host must not be blank.", error.message)
        }
    }

    @Test
    fun `rejects invalid port`() {
        try {
            SshHostEndpoint(
                serverId = "server-1",
                host = "example.com",
                port = 0,
            )
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("SSH port must be between 1 and 65535.", error.message)
        }
    }
}
