package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SshjConnectionServiceTest {

    @Test
    fun `returns unsupported configuration before real connection behavior exists`() = runBlocking {
        val service = sshjConnectionService()

        val result = service.connect(connectionRequest())

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            result,
        )
    }

    @Test
    fun `clears mapped authentication input after unsupported shell result`() = runBlocking {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val service = sshjConnectionService()

        service.connect(
            connectionRequest(authenticationInput = authenticationInput),
        )

        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }

    private fun sshjConnectionService(): SshjConnectionService {
        return SshjConnectionService(
            authenticationAdapter = SshjAuthenticationAdapter(),
        )
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
