package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjAuthenticationExecutorTest {

    private val adapter = SshjAuthenticationAdapter()
    private val executor = SshjAuthenticationExecutor()

    @Test
    fun `returns authentication required for missing authentication input`() {
        val client = FakeAuthenticatedClient()
        val mapping = adapter.map(connectionRequest())

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.AuthenticationRequired, result)
        assertEquals(0, client.authPasswordCallCount)
    }

    @Test
    fun `returns authentication required for blank password input`() {
        val client = FakeAuthenticatedClient()
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.Password(""),
            ),
        )

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.AuthenticationRequired, result)
        assertEquals(0, client.authPasswordCallCount)
    }

    @Test
    fun `executes password authentication against provided client`() {
        val client = FakeAuthenticatedClient()
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.Password("secret-password"),
            ),
        )

        val result = executor.authenticate(client, mapping)

        assertTrue(result is SshjAuthenticationExecutionResult.Authenticated)
        assertEquals(1, client.authPasswordCallCount)
        assertEquals("admin", client.lastUsername)
        assertEquals("secret-password", client.lastPassword)
    }

    @Test
    fun `maps password authentication failure to authentication required`() {
        val client = FakeAuthenticatedClient(
            authPasswordError = SshjAuthenticationFailedException(),
        )
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.Password("secret-password"),
            ),
        )

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.AuthenticationRequired, result)
    }

    @Test
    fun `maps unexpected password authentication failure to unknown`() {
        val client = FakeAuthenticatedClient(
            authPasswordError = IllegalStateException("Unexpected authentication failure"),
        )
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.Password("secret-password"),
            ),
        )

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.Unknown, result)
    }

    @Test
    fun `returns unsupported configuration for private key passphrase without private key material`() {
        val client = FakeAuthenticatedClient()
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.PrivateKeyPassphrase("secret-passphrase"),
            ),
        )

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.UnsupportedConfiguration, result)
        assertEquals(0, client.authPasswordCallCount)
    }

    private fun assertFailed(
        expectedError: SshConnectionError,
        result: SshjAuthenticationExecutionResult,
    ) {
        val failed = result as SshjAuthenticationExecutionResult.Failed
        assertEquals(expectedError, failed.error)
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

    private class FakeAuthenticatedClient(
        private val authPasswordError: RuntimeException? = null,
    ) : SshjAuthenticatedClient {

        var authPasswordCallCount = 0
            private set

        var lastUsername = ""
            private set

        var lastPassword = ""
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
