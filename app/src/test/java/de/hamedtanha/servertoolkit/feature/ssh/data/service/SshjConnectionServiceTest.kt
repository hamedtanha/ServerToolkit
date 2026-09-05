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
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
    fun `cancellation after authenticated owner creation discards registered session`() = runBlocking {
        val registry = SshjSessionOwnerRegistry()
        val executor = FakeTrustedConnectionExecutor(
            onBeforeConnectedResult = {
                currentCoroutineContext().cancel(
                    CancellationException("cancelled before delivery"),
                )
            },
        )
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = executor,
            sessionOwnerRegistry = registry,
        )
        var observedCancellation: CancellationException? = null

        val job = launch {
            try {
                service.connect(connectionRequest())
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                observedCancellation = error
            }
        }
        job.join()

        assertEquals("cancelled before delivery", observedCancellation?.message)
        assertTrue(executor.ownerCloseAttempted)
        assertTrue(executor.ownerClosed)
        assertFalse(registry.contains(executor.ownerHandle))
    }

    @Test
    fun `cancellation after registry insertion before caller resumption discards session`() = runBlocking {
        val registry = SshjSessionOwnerRegistry()
        val executor = FakeTrustedConnectionExecutor()
        val callerDispatcher = QueueingDispatcher()
        val ioDispatcher = QueueingDispatcher()
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = executor,
            sessionOwnerRegistry = registry,
            ioDispatcher = ioDispatcher,
        )
        var observedCancellation: CancellationException? = null

        val job = launch(callerDispatcher) {
            try {
                service.connect(connectionRequest())
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                observedCancellation = error
            }
        }

        callerDispatcher.runNext()
        assertTrue(ioDispatcher.hasQueuedTask())

        ioDispatcher.runNext()
        assertTrue(registry.contains(executor.ownerHandle))
        assertTrue(callerDispatcher.hasQueuedTask())

        job.cancel(CancellationException("cancelled handoff"))
        callerDispatcher.runNext()

        assertTrue(ioDispatcher.hasQueuedTask())
        assertTrue(registry.contains(executor.ownerHandle))

        ioDispatcher.runNext()
        assertTrue(executor.ownerCloseAttempted)
        assertTrue(executor.ownerClosed)
        assertFalse(registry.contains(executor.ownerHandle))
        assertTrue(callerDispatcher.hasQueuedTask())

        callerDispatcher.runNext()
        job.join()

        assertEquals("cancelled handoff", observedCancellation?.message)
        assertTrue(executor.ownerCloseAttempted)
        assertTrue(executor.ownerClosed)
        assertFalse(registry.contains(executor.ownerHandle))
    }

    @Test
    fun `cancellation remains primary when undelivered session cleanup fails`() = runBlocking {
        val registry = SshjSessionOwnerRegistry()
        val executor = FakeTrustedConnectionExecutor(
            onBeforeConnectedResult = {
                currentCoroutineContext().cancel(
                    CancellationException("primary cancellation"),
                )
            },
            ownerCloseError = IllegalStateException("simulated cleanup failure"),
        )
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = executor,
            sessionOwnerRegistry = registry,
        )
        var observedCancellation: CancellationException? = null

        val job = launch {
            try {
                service.connect(connectionRequest())
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                observedCancellation = error
            }
        }
        job.join()

        assertEquals("primary cancellation", observedCancellation?.message)
        assertTrue(executor.ownerCloseAttempted)
        assertFalse(registry.contains(executor.ownerHandle))
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

    @Test
    fun `maps unexpected trusted executor exception to unknown and clears authentication input`() = runBlocking {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = FakeTrustedConnectionExecutor(
                error = IllegalStateException("unexpected executor failure"),
            ),
        )

        val result = service.connect(
            connectionRequest(authenticationInput = authenticationInput),
        )

        assertEquals(
            SshConnectionResult.Failed(SshConnectionError.Unknown),
            result,
        )
        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }

    @Test
    fun `preserves trusted executor cancellation and clears authentication input`() = runBlocking {
        val authenticationInput = SshAuthenticationInput.Password("secret-password")
        val service = sshjConnectionService(
            hostTrustRepository = FakeSshHostTrustRepository(trustedHostKey()),
            trustedConnectionExecutor = FakeTrustedConnectionExecutor(
                error = CancellationException("cancelled connection"),
            ),
        )

        try {
            service.connect(
                connectionRequest(authenticationInput = authenticationInput),
            )
            throw AssertionError("Expected CancellationException to be thrown.")
        } catch (error: CancellationException) {
            assertEquals("cancelled connection", error.message)
        }

        assertFalse(authenticationInput.hasSensitiveValue)
        assertEquals("", authenticationInput.password)
    }

    private fun sshjConnectionService(
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(),
        trustedConnectionExecutor: SshjTrustedConnectionExecutor = FakeTrustedConnectionExecutor(),
        sessionOwnerRegistry: SshjSessionOwnerRegistry = SshjSessionOwnerRegistry(),
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): SshjConnectionService {
        return SshjConnectionService(
            authenticationAdapter = SshjAuthenticationAdapter(),
            hostTrustRepository = hostTrustRepository,
            trustedConnectionExecutor = trustedConnectionExecutor,
            sessionOwnerRegistry = sessionOwnerRegistry,
            ioDispatcher = ioDispatcher,
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

    private class QueueingDispatcher : CoroutineDispatcher() {

        private val tasks = LinkedBlockingQueue<Runnable>()

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            tasks.put(block)
        }

        fun hasQueuedTask(): Boolean {
            return tasks.isNotEmpty()
        }

        fun runNext() {
            val task = tasks.poll()
            if (task == null) {
                fail("Expected queued coroutine continuation")
                return
            }
            task.run()
        }
    }

    private class FakeTrustedConnectionExecutor(
        private val result: SshjTrustedConnectionExecutionResult? = null,
        private val error: RuntimeException? = null,
        private val onBeforeConnectedResult: suspend () -> Unit = {},
        private val ownerCloseError: RuntimeException? = null,
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

        @Volatile
        var ownerCloseAttempted = false
            private set

        @Volatile
        var ownerClosed = false
            private set

        var lastRequest: SshConnectionRequest? = null
            private set

        var lastTrustedHostKey: SshTrustedHostKey? = null
            private set

        var lastAuthenticationMapping: SshjAuthenticationMapping? = null
            private set

        override suspend fun connectAndAuthenticate(
            request: SshConnectionRequest,
            trustedHostKey: SshTrustedHostKey,
            authenticationMapping: SshjAuthenticationMapping,
        ): SshjTrustedConnectionExecutionResult {
            connectAndAuthenticateCallCount += 1
            lastRequest = request
            lastTrustedHostKey = trustedHostKey
            lastAuthenticationMapping = authenticationMapping

            error?.let { throw it }

            val executionResult = result ?: SshjTrustedConnectionExecutionResult.Connected(
                SshjSessionOwner(
                    sessionHandle = ownerHandle,
                    closeAction = {
                        ownerCloseAttempted = true
                        ownerCloseError?.let { throw it }
                        ownerClosed = true
                    },
                ),
            )

            if (executionResult is SshjTrustedConnectionExecutionResult.Connected) {
                onBeforeConnectedResult()
            }

            return executionResult
        }
    }
}
