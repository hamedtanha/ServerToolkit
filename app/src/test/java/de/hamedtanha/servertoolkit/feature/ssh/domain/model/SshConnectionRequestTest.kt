package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SshConnectionRequestTest {

    @Test
    fun `stores non-sensitive connection target data`() {
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
    }
}
