package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshConnectionAttemptUseCaseTest {

    @Test
    fun `connects using resolved target metadata when observed host key is trusted`() = runBlocking {
        val service = FakeSshConnectionService(sshConnectedResult())
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
        )

        val outcome = useCase("server-1")

        assertConnectionResult(sshConnectedResult(), outcome)
        assertEquals("server-1", service.lastRequest?.serverId)
        assertEquals("example.com", service.lastRequest?.host)
        assertEquals(2222, service.lastRequest?.port)
        assertEquals("admin", service.lastRequest?.username)
        assertTrue(service.discardedUndeliveredSessions.isEmpty())
    }

    @Test
    fun `passes authentication input to connection request and clears it after trusted attempt`() =
        runBlocking {
            var observedSensitiveInput = false
            var observedPassword = ""
            val authenticationInput = SshAuthenticationInput.Password("secret-password")
            val service = FakeSshConnectionService(
                result = sshConnectedResult(),
                onConnect = { request ->
                    val passwordInput = request.authenticationInput as SshAuthenticationInput.Password
                    observedSensitiveInput = passwordInput.hasSensitiveValue
                    observedPassword = passwordInput.password
                    assertFalse(request.toString().contains("secret-password"))
                },
            )
            val useCase = createUseCase(
                resolver = FakeConnectionTargetResolver(resolvedTarget()),
                service = service,
                observationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Observed(observedHostKey()),
                ),
                hostTrustRepository = FakeSshHostTrustRepository(
                    initialTrustedHostKey = trustedHostKey(),
                ),
            )

            val outcome = useCase(
                serverId = "server-1",
                authenticationInput = authenticationInput,
            )

            assertConnectionResult(sshConnectedResult(), outcome)
            assertTrue(observedSensitiveInput)
            assertEquals("secret-password", observedPassword)
            assertFalse(authenticationInput.hasSensitiveValue)
            assertFalse(service.lastRequest.toString().contains("secret-password"))
        }

    @Test
    fun `returns review decision and does not connect when observed host key is unknown`() =
        runBlocking {
            val service = FakeSshConnectionService(sshConnectedResult())
            val useCase = createUseCase(
                resolver = FakeConnectionTargetResolver(resolvedTarget()),
                service = service,
                observationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Observed(observedHostKey()),
                ),
            )

            val outcome = useCase("server-1")

            val decision = assertHostTrustDecision(outcome)
            assertTrue(decision is SshHostTrustDecision.ReviewRequired)
            assertEquals(0, service.connectCallCount)
        }

    @Test
    fun `returns changed host key decision and does not connect when observed host key changed`() =
        runBlocking {
            val service = FakeSshConnectionService(sshConnectedResult())
            val useCase = createUseCase(
                resolver = FakeConnectionTargetResolver(resolvedTarget()),
                service = service,
                observationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Observed(
                        observedHostKey(
                            fingerprint = fingerprint(value = "observed-fingerprint"),
                        ),
                    ),
                ),
                hostTrustRepository = FakeSshHostTrustRepository(
                    initialTrustedHostKey = trustedHostKey(
                        fingerprint = fingerprint(value = "trusted-fingerprint"),
                    ),
                ),
            )

            val outcome = useCase("server-1")

            val decision = assertHostTrustDecision(outcome)
            assertTrue(decision is SshHostTrustDecision.BlockedChangedHostKey)
            assertEquals(0, service.connectCallCount)
        }

    @Test
    fun `maps unavailable host key observation to unsupported configuration without connecting`() =
        runBlocking {
            val service = FakeSshConnectionService(sshConnectedResult())
            val useCase = createUseCase(
                resolver = FakeConnectionTargetResolver(resolvedTarget()),
                service = service,
                observationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Unavailable,
                ),
            )

            val outcome = useCase("server-1")

            assertFailure(SshConnectionError.UnsupportedConfiguration, outcome)
            assertEquals(0, service.connectCallCount)
        }

    @Test
    fun `maps missing target to target not found failure`() = runBlocking {
        val service = FakeSshConnectionService(sshConnectedResult())
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(ConnectionTargetResolution.NotFound),
            service = service,
        )

        val outcome = useCase("missing-server")

        assertFailure(SshConnectionError.TargetNotFound, outcome)
        assertEquals(0, service.connectCallCount)
    }

    @Test
    fun `maps invalid target to missing metadata failure`() = runBlocking {
        val service = FakeSshConnectionService(sshConnectedResult())
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(
                ConnectionTargetResolution.Invalid(ConnectionTargetInvalidReason.MissingUsername),
            ),
            service = service,
        )

        val outcome = useCase("server-1")

        assertFailure(SshConnectionError.MissingConnectionMetadata, outcome)
        assertEquals(0, service.connectCallCount)
    }

    @Test
    fun `maps timeout to connection timeout failure`() = runBlocking {
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                delay(100)
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
            timeoutMillis = 10,
        )

        val outcome = useCase("server-1")

        assertFailure(SshConnectionError.ConnectionTimeout, outcome)
        assertTrue(service.discardedUndeliveredSessions.isEmpty())
    }

    @Test
    fun `timeout after connected result becomes available discards undelivered session`() = runBlocking {
        val connectedResult = sshConnectedResult(sessionId = "late-session")
        val service = FakeSshConnectionService(
            result = connectedResult,
            onConnect = {
                withContext(NonCancellable) {
                    delay(50)
                }
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
            timeoutMillis = 10,
        )

        val outcome = useCase("server-1")

        assertFailure(SshConnectionError.ConnectionTimeout, outcome)
        assertEquals(
            listOf(connectedResult.sessionHandle),
            service.discardedUndeliveredSessions,
        )
    }

    @Test
    fun `cancellation after connected result discards undelivered session`() = runBlocking {
        val connectedResult = sshConnectedResult(sessionId = "cancelled-session")
        val service = FakeSshConnectionService(
            result = connectedResult,
            onConnect = {
                currentCoroutineContext().cancel(
                    CancellationException("Connection cancelled after connect"),
                )
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
        )
        var observedCancellation: CancellationException? = null

        val job = launch {
            try {
                useCase("server-1")
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                observedCancellation = error
            }
        }
        job.join()

        assertEquals("Connection cancelled after connect", observedCancellation?.message)
        assertEquals(
            listOf(connectedResult.sessionHandle),
            service.discardedUndeliveredSessions,
        )
    }

    @Test
    fun `cleanup failure does not replace cancellation after connected result`() = runBlocking {
        val connectedResult = sshConnectedResult(sessionId = "cleanup-failure-session")
        val service = FakeSshConnectionService(
            result = connectedResult,
            onConnect = {
                currentCoroutineContext().cancel(
                    CancellationException("Primary cancellation"),
                )
            },
            onDiscardUndeliveredSession = {
                throw IllegalStateException("Simulated rollback failure")
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
        )
        var observedCancellation: CancellationException? = null

        val job = launch {
            try {
                useCase("server-1")
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                observedCancellation = error
            }
        }
        job.join()

        assertEquals("Primary cancellation", observedCancellation?.message)
        assertEquals(
            listOf(connectedResult.sessionHandle),
            service.discardedUndeliveredSessions,
        )
    }

    @Test
    fun `maps unexpected exception to unknown failure`() = runBlocking {
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                throw IllegalStateException("Unexpected failure")
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
        )

        val outcome = useCase("server-1")

        assertFailure(SshConnectionError.Unknown, outcome)
    }

    @Test
    fun `preserves coroutine cancellation`() = runBlocking {
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                throw CancellationException("Connection cancelled")
            },
        )
        val useCase = createUseCase(
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
            observationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustRepository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey(),
            ),
        )

        try {
            useCase("server-1")
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }
        assertTrue(service.discardedUndeliveredSessions.isEmpty())
    }

    private fun createUseCase(
        resolver: FakeConnectionTargetResolver,
        service: FakeSshConnectionService,
        observationService: FakeSshHostKeyObservationService = FakeSshHostKeyObservationService(),
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(),
        timeoutMillis: Long = 1_000,
    ): SshConnectionAttemptUseCase {
        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(hostTrustRepository),
        )

        return SshConnectionAttemptUseCase(
            connectionTargetResolver = resolver,
            connectionService = service,
            hostKeyObservationService = observationService,
            hostTrustDecisionUseCase = hostTrustDecisionUseCase,
            connectionHistoryRepository = FakeSshConnectionHistoryRepository(),
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

    private fun assertConnectionResult(
        expectedResult: SshConnectionResult,
        outcome: SshConnectionAttemptOutcome,
    ) {
        val connectionOutcome = outcome as SshConnectionAttemptOutcome.ConnectionResult
        assertEquals(expectedResult, connectionOutcome.result)
    }

    private fun assertFailure(
        expectedError: SshConnectionError,
        outcome: SshConnectionAttemptOutcome,
    ) {
        val connectionOutcome = outcome as SshConnectionAttemptOutcome.ConnectionResult
        val failure = connectionOutcome.result as SshConnectionResult.Failed
        assertEquals(expectedError, failure.error)
    }

    private fun assertHostTrustDecision(
        outcome: SshConnectionAttemptOutcome,
    ): SshHostTrustDecision {
        return (outcome as SshConnectionAttemptOutcome.HostTrustDecisionRequired).decision
    }

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 2222,
        )
    }

    private fun fingerprint(
        value: String = "abc123",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = value,
        )
    }

    private fun observedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }

    private fun trustedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }
}
