package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SshjConnectionServiceTest {

    @Test
    fun `returns host trust required when trusted host key is missing`() = runBlocking {
        val executor = FakeTrustedConnectionExecutor()
        val service = sshjConnectionService(
            trustedConnectionExecutor = executor,
        )

        val result = service.connect(connectionRequest())

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.HostTrustRequired),
            result,
        )
        assertEquals(0, executor.connectCallCount)
    }

    @Test
    fun `executes trusted connection when trusted host key exists`() = runBlocking {
        val trustedHostKey = trustedHostKey()
        val executor = FakeTrustedConnectionExecutor()
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey),
            trustedConnectionExecutor = executor,
        )
        val request = connectionRequest()

        val result = service.connect(request)

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            result,
        )
        assertEquals(1, executor.connectCallCount)
        assertEquals(request, executor.lastRequest)
        assertEquals(trustedHostKey, executor.lastTrustedHostKey)
    }

    @Test
    fun `maps trusted connection execution failure to connection failure`() = runBlocking {
        val executor = FakeTrustedConnectionExecutor(
            result = SshjTrustedConnectionExecutionResult.Failed(
                SshConnectionError.ConnectionTimeout,
            ),
        )
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = executor,
        )

        val result = service.connect(connectionRequest())

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout),
            result,
        )
    }

    @Test
    fun `clears mapped authentication input when host trust is missing`() = runBlocking {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val service = sshjConnectionService()

        service.connect(
            connectionRequest(authenticationInput = authenticationInput),
        )

        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }

    @Test
    fun `clears mapped authentication input after trusted connection execution`() = runBlocking {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
        )

        service.connect(
            connectionRequest(authenticationInput = authenticationInput),
        )

        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }

    private fun sshjConnectionService(
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(),
        trustedConnectionExecutor: SshjTrustedConnectionExecutor = FakeTrustedConnectionExecutor(),
    ): SshjConnectionService {
        return SshjConnectionService(
            authenticationAdapter = SshjAuthenticationAdapter(),
            hostTrustRepository = hostTrustRepository,
            trustedConnectionExecutor = trustedConnectionExecutor,
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

    private fun trustedHostKey(): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = SshHostEndpoint(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
            fingerprint = SshHostKeyFingerprint(
                algorithm = "MD5",
                value = "MD5:fingerprint",
            ),
        )
    }

    private class FakeTrustedConnectionExecutor(
        private val result: SshjTrustedConnectionExecutionResult =
            SshjTrustedConnectionExecutionResult.Connected,
    ) : SshjTrustedConnectionExecutor {

        var connectCallCount = 0
            private set

        var lastRequest: SshConnectionRequest? = null
            private set

        var lastTrustedHostKey: SshTrustedHostKey? = null
            private set

        override fun connect(
            request: SshConnectionRequest,
            trustedHostKey: SshTrustedHostKey,
        ): SshjTrustedConnectionExecutionResult {
            connectCallCount += 1
            lastRequest = request
            lastTrustedHostKey = trustedHostKey
            return result
        }
    }
}
