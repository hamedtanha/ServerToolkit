package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeyMaterial
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshjAuthenticationExecutorTest {

    private val adapter = SshjAuthenticationAdapter()
    private val executor = SshjAuthenticationExecutor()

    @Test
    fun `returns authentication required for missing authentication input`() = runTest {
        val client = FakeAuthenticatedClient()
        val mapping = adapter.map(connectionRequest())

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.AuthenticationRequired, result)
        assertEquals(0, client.authPasswordCallCount)
    }

    @Test
    fun `returns authentication required for blank password input`() = runTest {
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
    fun `executes password authentication against provided client`() = runTest {
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
    fun `maps password authentication failure to authentication required`() = runTest {
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
    fun `maps unexpected password authentication failure to unknown`() = runTest {
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
    fun `preserves password authentication cancellation`() = runTest {
        val client = FakeAuthenticatedClient(
            authPasswordError = CancellationException("cancelled"),
        )
        val mapping = adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.Password("secret-password"),
            ),
        )

        try {
            executor.authenticate(client, mapping)
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }

        assertEquals(1, client.authPasswordCallCount)
    }

    @Test
    fun `consumes private key once authenticates and clears material`() = runTest {
        val expectedPrivateKey = "test-private-key".toByteArray()
        val source = ConsumablePrivateKeySource(expectedPrivateKey.copyOf())
        val client = FakeAuthenticatedClient()
        val mapping = privateKeyMapping(
            source = source,
            passphrase = "secret-passphrase",
        )

        val result = executor.authenticate(client, mapping)

        assertTrue(result is SshjAuthenticationExecutionResult.Authenticated)
        assertEquals(1, source.consumeCallCount)
        assertEquals(1, client.authPrivateKeyCallCount)
        assertEquals("admin", client.lastPrivateKeyUsername)
        assertEquals(expectedPrivateKey.size, client.lastPrivateKeySize)
        assertEquals("secret-passphrase", client.lastPrivateKeyPassphrase)
        assertArrayEquals(expectedPrivateKey, client.lastPrivateKeySnapshot)
        assertTrue(
            client.lastPrivateKeyBuffer
                ?.all { byte -> byte == 0.toByte() }
                ?: false,
        )
    }

    @Test
    fun `returns private key unavailable when source was already transferred`() = runTest {
        val source = ConsumablePrivateKeySource("test-private-key".toByteArray())
        val client = FakeAuthenticatedClient()
        val mapping = privateKeyMapping(source)

        mapping.takePrivateKeySource()

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.PrivateKeyUnavailable, result)
        assertEquals(0, source.consumeCallCount)
        assertEquals(0, client.authPrivateKeyCallCount)
    }

    @Test
    fun `maps private key source failures to stable connection errors`() = runTest {
        val cases = listOf(
            SshPrivateKeySourceError.AlreadyConsumed to
                SshConnectionError.PrivateKeyUnavailable,
            SshPrivateKeySourceError.Invalidated to
                SshConnectionError.PrivateKeyUnavailable,
            SshPrivateKeySourceError.DocumentUnavailable to
                SshConnectionError.PrivateKeyUnavailable,
            SshPrivateKeySourceError.ReadFailed to
                SshConnectionError.PrivateKeyUnavailable,
            SshPrivateKeySourceError.EmptyDocument to
                SshConnectionError.PrivateKeyEmpty,
            SshPrivateKeySourceError.DocumentTooLarge to
                SshConnectionError.PrivateKeyTooLarge,
        )

        cases.forEach { (sourceError, expectedError) ->
            val client = FakeAuthenticatedClient()
            val mapping = privateKeyMapping(
                FailingPrivateKeySource(sourceError),
            )

            val result = executor.authenticate(client, mapping)

            assertFailed(expectedError, result)
            assertEquals(0, client.authPrivateKeyCallCount)
        }
    }

    @Test
    fun `maps private key authentication failures to stable connection errors`() = runTest {
        val cases = listOf(
            SshjPrivateKeyAuthenticationException.UnsupportedFormat() to
                SshConnectionError.PrivateKeyUnsupportedFormat,
            SshjPrivateKeyAuthenticationException.InvalidKey() to
                SshConnectionError.PrivateKeyInvalid,
            SshjPrivateKeyAuthenticationException.PassphraseRequired() to
                SshConnectionError.PrivateKeyPassphraseRequired,
            SshjPrivateKeyAuthenticationException.PassphraseRejected() to
                SshConnectionError.PrivateKeyPassphraseRejected,
            SshjPrivateKeyAuthenticationException.AuthenticationRejected() to
                SshConnectionError.AuthenticationRejected,
        )

        cases.forEach { (authenticationError, expectedError) ->
            val source = ConsumablePrivateKeySource("test-private-key".toByteArray())
            val client = FakeAuthenticatedClient(
                authPrivateKeyError = authenticationError,
            )
            val mapping = privateKeyMapping(source)

            val result = executor.authenticate(client, mapping)

            assertFailed(expectedError, result)
            assertEquals(1, source.consumeCallCount)
            assertEquals(1, client.authPrivateKeyCallCount)
            assertTrue(
                client.lastPrivateKeyBuffer
                    ?.all { byte -> byte == 0.toByte() }
                    ?: false,
            )
        }
    }

    @Test
    fun `maps unexpected private key failure to unknown`() = runTest {
        val source = ConsumablePrivateKeySource("test-private-key".toByteArray())
        val client = FakeAuthenticatedClient(
            authPrivateKeyError = IllegalStateException("unexpected"),
        )
        val mapping = privateKeyMapping(source)

        val result = executor.authenticate(client, mapping)

        assertFailed(SshConnectionError.Unknown, result)
        assertTrue(
            client.lastPrivateKeyBuffer
                ?.all { byte -> byte == 0.toByte() }
                ?: false,
        )
    }

    @Test
    fun `preserves private key authentication cancellation and clears material`() = runTest {
        val source = ConsumablePrivateKeySource("test-private-key".toByteArray())
        val client = FakeAuthenticatedClient(
            authPrivateKeyError = CancellationException("cancelled private key"),
        )
        val mapping = privateKeyMapping(source)

        try {
            executor.authenticate(client, mapping)
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled private key", error.message)
        }

        assertEquals(1, source.consumeCallCount)
        assertEquals(1, client.authPrivateKeyCallCount)
        assertTrue(
            client.lastPrivateKeyBuffer
                ?.all { byte -> byte == 0.toByte() }
                ?: false,
        )
    }

    private fun privateKeyMapping(
        source: SshPrivateKeySource,
        passphrase: String = "",
    ): SshjAuthenticationMapping.PrivateKey {
        return adapter.map(
            connectionRequest(
                authenticationInput = SshAuthenticationInput.PrivateKey(
                    privateKeySource = source,
                    passphrase = passphrase,
                ),
            ),
        ) as SshjAuthenticationMapping.PrivateKey
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
        private val authPrivateKeyError: RuntimeException? = null,
    ) : SshjAuthenticatedClient {

        var authPasswordCallCount = 0
            private set

        var authPrivateKeyCallCount = 0
            private set

        var lastUsername = ""
            private set

        var lastPassword = ""
            private set

        var lastPrivateKeyUsername = ""
            private set

        var lastPrivateKeyBuffer: ByteArray? = null
            private set

        var lastPrivateKeySnapshot = byteArrayOf()
            private set

        var lastPrivateKeySize = 0
            private set

        var lastPrivateKeyPassphrase = ""
            private set

        override fun authPrivateKey(
            username: String,
            privateKeyBytes: ByteArray,
            privateKeySize: Int,
            passphrase: String,
        ) {
            authPrivateKeyCallCount += 1
            lastPrivateKeyUsername = username
            lastPrivateKeyBuffer = privateKeyBytes
            lastPrivateKeySnapshot = privateKeyBytes.copyOfRange(
                fromIndex = 0,
                toIndex = privateKeySize,
            )
            lastPrivateKeySize = privateKeySize
            lastPrivateKeyPassphrase = passphrase

            authPrivateKeyError?.let { throw it }
        }

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

    private class ConsumablePrivateKeySource(
        private val privateKeyBytes: ByteArray,
    ) : SshPrivateKeySource {

        var consumeCallCount = 0
            private set

        override suspend fun <T> consume(
            block: suspend SshPrivateKeyMaterial.() -> T,
        ): SshPrivateKeySourceResult<T> {
            consumeCallCount += 1
            val material = SshPrivateKeyMaterial(
                bytes = privateKeyBytes,
                validSize = privateKeyBytes.size,
            )

            return try {
                SshPrivateKeySourceResult.Success(material.block())
            } finally {
                material.clear()
            }
        }

        override fun invalidate(): Boolean = false
    }

    private class FailingPrivateKeySource(
        private val error: SshPrivateKeySourceError,
    ) : SshPrivateKeySource {

        override suspend fun <T> consume(
            block: suspend SshPrivateKeyMaterial.() -> T,
        ): SshPrivateKeySourceResult<T> {
            return SshPrivateKeySourceResult.Failure(error)
        }

        override fun invalidate(): Boolean = false
    }
}
