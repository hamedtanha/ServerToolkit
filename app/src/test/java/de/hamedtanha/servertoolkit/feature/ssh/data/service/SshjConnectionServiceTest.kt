package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertEquals(0, executor.connectAndAuthenticateCallCount)
    }

    @Test
    fun `registers session owner and returns connected result after trusted authentication`() = runBlocking {
        val trustedHostKey = trustedHostKey()
        val registry = SshjSessionOwnerRegistry()
        val executor = FakeTrustedConnectionExecutor()
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey),
            trustedConnectionExecutor = executor,
            sessionOwnerRegistry = registry,
        )
        val request = connectionRequest()

        val result = service.connect(request)

        assertTrue(result is SshConnectionResult.Connected)
        val connected = result as SshConnectionResult.Connected
        assertEquals(executor.ownerHandle, connected.sessionHandle)
        assertEquals(1, executor.connectAndAuthenticateCallCount)
        assertEquals(request, executor.lastRequest)
        assertEquals(trustedHostKey, executor.lastTrustedHostKey)
        assertTrue(executor.lastAuthenticationMapping is SshjAuthenticationMapping.None)
        assertTrue(registry.contains(connected.sessionHandle))
        assertFalse(executor.ownerClosed)

        val closeResult = registry.close(connected.sessionHandle)

        assertEquals(SshSessionCloseResult.Closed, closeResult)
        assertTrue(executor.ownerClosed)
    }

    @Test
    fun `maps trusted connection authentication failure to connection failure`() = runBlocking {
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
    fun `closes unregistered owner when session registration fails`() = runBlocking {
        val duplicateHandle = sessionHandle(sessionId = "duplicate-session")
        val registry = SshjSessionOwnerRegistry()
        registry.register(
            SshjSessionOwner(
                sessionHandle = duplicateHandle,
                closeAction = {},
            ),
        )
        val executor = FakeTrustedConnectionExecutor(
            ownerHandle = duplicateHandle,
        )
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = executor,
            sessionOwnerRegistry = registry,
        )

        val result = service.connect(connectionRequest())

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.Unknown),
            result,
        )
        assertTrue(executor.ownerClosed)
        assertTrue(registry.contains(duplicateHandle))
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
    fun `clears mapped authentication input after connected result`() = runBlocking {
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
        sessionOwnerRegistry: SshjSessionOwnerRegistry = SshjSessionOwnerRegistry(),
    ): SshjConnectionService {
        return SshjConnectionService(
            authenticationAdapter = SshjAuthenticationAdapter(),
            hostTrustRepository = hostTrustRepository,
            trustedConnectionExecutor = trustedConnectionExecutor,
            sessionOwnerRegistry = sessionOwnerRegistry,
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

    private fun sessionHandle(
        sessionId: String = "session-1",
    ): SshSessionHandle {
        return SshSessionHandle(
            sessionId = sessionId,
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }

    private class FakeTrustedConnectionExecutor(
        private val result: SshjTrustedConnectionExecutionResult? = null,
        val ownerHandle: SshSessionHandle = SshSessionHandle(
            sessionId = "session-1",
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        ),
    ) : SshjTrustedConnectionExecutor {

        var connectAndAuthenticateCallCount = 0
            private set

        var ownerClosed = false
            private set

        var lastRequest: SshConnectionRequest? = null
            private set

        var lastTrustedHostKey: SshTrustedHostKey? = null
            private set

        var lastAuthenticationMapping: SshjAuthenticationMapping? = null
            private set

        override fun connectAndAuthenticate(
            request: SshConnectionRequest,
            trustedHostKey: SshTrustedHostKey,
            authenticationMapping: SshjAuthenticationMapping,
        ): SshjTrustedConnectionExecutionResult {
            connectAndAuthenticateCallCount += 1
            lastRequest = request
            lastTrustedHostKey = trustedHostKey
            lastAuthenticationMapping = authenticationMapping

            return result ?: SshjTrustedConnectionExecutionResult.Connected(
                SshjSessionOwner(
                    sessionHandle = ownerHandle,
                    closeAction = {
                        ownerClosed = true
                    },
                ),
            )
        }
    }
}
