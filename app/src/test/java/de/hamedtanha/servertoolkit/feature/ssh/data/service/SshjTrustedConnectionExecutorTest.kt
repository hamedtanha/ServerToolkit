package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjTrustedConnectionExecutorTest {

    private val authenticationAdapter = SshjAuthenticationAdapter()
    private val authenticationExecutor = SshjAuthenticationExecutor()

    @Test
    fun `authenticated result remains represented as authenticated boundary result`() {
        val client = FakeAuthenticatedClient()
        val result = authenticationExecutor.authenticate(
            client = client,
            mapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertTrue(result is SshjAuthenticationExecutionResult.Authenticated)
        assertEquals(1, client.authPasswordCallCount)
        assertEquals("admin", client.lastUsername)
        assertEquals("secret-password", client.lastPassword)
    }

    @Test
    fun `authentication failure is mapped to authentication required`() {
        val client = FakeAuthenticatedClient(
            authPasswordError = SshjAuthenticationFailedException(),
        )

        val result = authenticationExecutor.authenticate(
            client = client,
            mapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        val failed = result as SshjAuthenticationExecutionResult.Failed
        assertEquals(SshConnectionError.AuthenticationRequired, failed.error)
    }

    @Test
    fun `missing password is mapped to authentication required`() {
        val client = FakeAuthenticatedClient()

        val result = authenticationExecutor.authenticate(
            client = client,
            mapping = authenticationAdapter.map(connectionRequest()),
        )

        val failed = result as SshjAuthenticationExecutionResult.Failed
        assertEquals(SshConnectionError.AuthenticationRequired, failed.error)
        assertEquals(0, client.authPasswordCallCount)
    }

    @Test
    fun `trusted connection failure closes client and maps unknown host`() {
        val client = FakeTrustedConnectionClient(
            connectError = UnknownHostException("host not found"),
        )
        val executor = trustedConnectionExecutor(client)

        val result = executor.connectAndAuthenticate(
            request = connectionRequest(),
            trustedHostKey = trustedHostKey(),
            authenticationMapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertEquals(
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.UnknownHost),
            result,
        )
        assertEquals(1, client.connectCallCount)
        assertEquals(1, client.closeCallCount)
        assertTrue(client.closed)
    }

    @Test
    fun `trusted connection timeout closes client and maps connection timeout`() {
        val client = FakeTrustedConnectionClient(
            connectError = SocketTimeoutException("connect timed out"),
        )
        val executor = trustedConnectionExecutor(client)

        val result = executor.connectAndAuthenticate(
            request = connectionRequest(),
            trustedHostKey = trustedHostKey(),
            authenticationMapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertEquals(
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.ConnectionTimeout),
            result,
        )
        assertEquals(1, client.closeCallCount)
        assertTrue(client.closed)
    }

    @Test
    fun `authentication failure closes client before returning failure`() {
        val client = FakeTrustedConnectionClient(
            authPasswordError = SshjAuthenticationFailedException(),
        )
        val executor = trustedConnectionExecutor(client)

        val result = executor.connectAndAuthenticate(
            request = connectionRequest(),
            trustedHostKey = trustedHostKey(),
            authenticationMapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertEquals(
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.AuthenticationRequired),
            result,
        )
        assertEquals(1, client.connectCallCount)
        assertEquals(1, client.authPasswordCallCount)
        assertEquals(1, client.closeCallCount)
        assertTrue(client.closed)
    }

    @Test
    fun `authenticated connection transfers client ownership without closing immediately`() {
        val client = FakeTrustedConnectionClient()
        val executor = trustedConnectionExecutor(client)

        val result = executor.connectAndAuthenticate(
            request = connectionRequest(),
            trustedHostKey = trustedHostKey(),
            authenticationMapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertTrue(result is SshjTrustedConnectionExecutionResult.Connected)
        assertEquals(1, client.connectCallCount)
        assertEquals(1, client.authPasswordCallCount)
        assertEquals(0, client.closeCallCount)
        assertFalse(client.closed)

        val connected = result as SshjTrustedConnectionExecutionResult.Connected
        connected.sessionOwner.close()

        assertEquals(1, client.closeCallCount)
        assertTrue(client.closed)
    }

    @Test
    fun `trusted connection cleanup failure is contained`() {
        val client = FakeTrustedConnectionClient(
            connectError = UnknownHostException("host not found"),
            closeError = IllegalStateException("close failed"),
        )
        val executor = trustedConnectionExecutor(client)

        val result = executor.connectAndAuthenticate(
            request = connectionRequest(),
            trustedHostKey = trustedHostKey(),
            authenticationMapping = authenticationAdapter.map(
                connectionRequest(
                    authenticationInput = SshAuthenticationInput.Password("secret-password"),
                ),
            ),
        )

        assertEquals(
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.UnknownHost),
            result,
        )
        assertEquals(1, client.closeCallCount)
    }

    @Test
    fun `preserves cancellation when trusted connection cleanup fails`() {
        val client = FakeTrustedConnectionClient(
            connectError = CancellationException("cancelled connection"),
            closeError = IllegalStateException("close failed"),
        )
        val executor = trustedConnectionExecutor(client)

        try {
            executor.connectAndAuthenticate(
                request = connectionRequest(),
                trustedHostKey = trustedHostKey(),
                authenticationMapping = authenticationAdapter.map(
                    connectionRequest(
                        authenticationInput = SshAuthenticationInput.Password("secret-password"),
                    ),
                ),
            )
            throw AssertionError("Expected CancellationException to be thrown.")
        } catch (error: CancellationException) {
            assertEquals("cancelled connection", error.message)
        }

        assertEquals(1, client.closeCallCount)
    }

    private fun trustedConnectionExecutor(
        client: FakeTrustedConnectionClient,
    ): SshjNetworkTrustedConnectionExecutor {
        return SshjNetworkTrustedConnectionExecutor(
            trustedHostKeyVerifierFactory = SshjTrustedHostKeyVerifierFactory(),
            authenticationExecutor = authenticationExecutor,
            clientFactory = FakeTrustedConnectionClientFactory(client),
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
                algorithm = "SHA256",
                value = "SHA256:fingerprint",
            ),
        )
    }

    private class FakeAuthenticatedClient(
        private val authPasswordError: RuntimeException? = null,
    ) : SshjAuthenticatedClient {

        var authPasswordCallCount = 0
            private set

        var lastUsername: String? = null
            private set

        var lastPassword: String? = null
            private set

        override fun authPassword(
            username: String,
            password: String,
        ) {
            authPasswordCallCount += 1
            lastUsername = username
            lastPassword = password
            authPasswordError?.let { throw it }
        }
    }

    private class FakeTrustedConnectionClientFactory(
        private val client: FakeTrustedConnectionClient,
    ) : SshjTrustedConnectionClientFactory {

        override fun createClient(): SshjTrustedConnectionClient {
            return client
        }
    }

    private class FakeTrustedConnectionClient(
        private val connectError: Exception? = null,
        private val authPasswordError: RuntimeException? = null,
        private val closeError: RuntimeException? = null,
    ) : SshjTrustedConnectionClient {

        override var connectTimeout: Int = 0

        override var timeout: Int = 0

        var connectCallCount = 0
            private set

        var authPasswordCallCount = 0
            private set

        var closeCallCount = 0
            private set

        var closed = false
            private set

        override fun addHostKeyVerifier(verifier: HostKeyVerifier) = Unit

        override fun connect(
            host: String,
            port: Int,
        ) {
            connectCallCount += 1
            connectError?.let { throw it }
        }

        override fun authPassword(
            username: String,
            password: String,
        ) {
            authPasswordCallCount += 1
            authPasswordError?.let { throw it }
        }

        override fun close() {
            closeCallCount += 1
            closeError?.let { throw it }
            closed = true
        }

        override fun asCommandChannelClient(): SshjCommandChannelClient {
            return object : SshjCommandChannelClient {
                override fun openCommandChannel(command: String): SshjCommandChannel {
                    error("Command execution is outside this test scope.")
                }
            }
        }
    }
}
