package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import org.junit.Assert.assertEquals
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

    @Suppress("unused")
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
}
