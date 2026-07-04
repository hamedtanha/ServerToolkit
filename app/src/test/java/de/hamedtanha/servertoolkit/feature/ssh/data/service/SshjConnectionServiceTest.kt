package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjConnectionServiceTest {

    @Test
    fun `returns unsupported configuration before real connection behavior exists`() = runBlocking {
        val service = SshjConnectionService()

        val result = service.connect(
            SshConnectionRequest(
                serverId = "server-1",
                host = "example.com",
                port = 22,
                username = "admin",
            ),
        )

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            result,
        )
    }
}
