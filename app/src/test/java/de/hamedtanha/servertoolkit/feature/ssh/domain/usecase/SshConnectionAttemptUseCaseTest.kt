package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshConnectionAttemptUseCaseTest {

    @Test
    fun `connects using resolved target metadata`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
        )

        val result = useCase("server-1")

        assertEquals(SshConnectionResult.Connected, result)
        assertEquals("server-1", service.lastRequest?.serverId)
        assertEquals("example.com", service.lastRequest?.host)
        assertEquals(2222, service.lastRequest?.port)
        assertEquals("admin", service.lastRequest?.username)
    }

    @Test
    fun `maps missing target to target not found failure`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(ConnectionTargetResolution.NotFound),
            service = service,
        )

        val result = useCase("missing-server")

        assertFailure(SshConnectionError.TargetNotFound, result)
        assertEquals(0, service.connectCallCount)
    }

    @Test
    fun `maps invalid target to missing metadata failure`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(
                ConnectionTargetResolution.Invalid(ConnectionTargetInvalidReason.MissingUsername),
            ),
            service = service,
        )

        val result = useCase("server-1")

        assertFailure(SshConnectionError.MissingConnectionMetadata, result)
        assertEquals(0, service.connectCallCount)
    }

    @Test
    fun `maps timeout to connection timeout failure`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
            onConnect = {
                delay(100)
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            timeoutMillis = 10,
        )

        val result = useCase("server-1")

        assertFailure(SshConnectionError.ConnectionTimeout, result)
    }

    @Test
    fun `maps unexpected exception to unknown failure`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
            onConnect = {
                throw IllegalStateException("Unexpected failure")
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
        )

        val result = useCase("server-1")

        assertFailure(SshConnectionError.Unknown, result)
    }

    @Test
    fun `preserves coroutine cancellation`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
            onConnect = {
                throw CancellationException("Connection cancelled")
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
        )

        try {
            useCase("server-1")
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }
    }

    private fun createUseCase(
        resolver: FakeConnectionTargetResolver,
        service: FakeSshConnectionService,
        timeoutMillis: Long = 1_000,
    ): SshConnectionAttemptUseCase {
        return SshConnectionAttemptUseCase(
            connectionTargetResolver = resolver,
            connectionService = service,
            timeoutMillis = timeoutMillis,
        )
    }

    private fun resolvedTarget(): ConnectionTargetResolution.Resolved {
        return ConnectionTargetResolution.Resolved(
            RemoteConnectionTarget(
                serverId = "server-1",
                host = "example.com",
                port = 2222,
                username = "admin",
            ),
        )
    }

    private fun assertFailure(
        expectedError: SshConnectionError,
        result: SshConnectionResult,
    ) {
        val failure = result as SshConnectionResult.Failed
        assertEquals(expectedError, failure.error)
    }
}
