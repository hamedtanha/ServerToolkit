package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeSshConnectionServiceTest {

    @Test
    fun `returns configured connection result`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
        )
        val request = connectionRequest()

        val result = service.connect(request)

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            result,
        )
    }

    @Test
    fun `stores last connection request`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
        )
        val request = connectionRequest()

        service.connect(request)

        assertEquals(request, service.lastRequest)
    }

    private fun connectionRequest(): SshConnectionRequest {
        return SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }
}
