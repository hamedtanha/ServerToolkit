package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshConnectionAttemptHistoryTest {

    @Test
    fun `records connected attempt with resolved target metadata`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            currentTimeMillisProvider = sequentialClock(1_000L, 2_000L),
        )

        useCase("server-1")

        val entry = historyRepository.savedEntries.single()
        assertEquals("history-1", entry.id)
        assertEquals("server-1", entry.serverId)
        assertEquals("example.com", entry.host)
        assertEquals(2222, entry.port)
        assertEquals("admin", entry.username)
        assertEquals(SshConnectionHistoryStatus.Connected, entry.status)
        assertEquals(1_000L, entry.attemptedAtEpochMillis)
        assertEquals(2_000L, entry.completedAtEpochMillis)
        assertEquals(null, entry.connectionError)
    }

    @Test
    fun `records failed connection result with domain error`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            service = FakeSshConnectionService(
                SshConnectionResult.Failed(SshConnectionError.AuthenticationRequired),
            ),
        )

        useCase("server-1")

        val entry = historyRepository.savedEntries.single()
        assertEquals(SshConnectionHistoryStatus.Failed, entry.status)
        assertEquals(SshConnectionError.AuthenticationRequired, entry.connectionError)
    }

    @Test
    fun `records timed out attempt with timeout error`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            service = FakeSshConnectionService(
                result = sshConnectedResult(),
                onConnect = { delay(100) },
            ),
            timeoutMillis = 10,
        )

        val outcome = useCase("server-1")

        val result = (outcome as SshConnectionAttemptOutcome.ConnectionResult).result
        val failure = result as SshConnectionResult.Failed
        assertEquals(SshConnectionError.ConnectionTimeout, failure.error)

        val entry = historyRepository.savedEntries.single()
        assertEquals(SshConnectionHistoryStatus.Failed, entry.status)
        assertEquals(SshConnectionError.ConnectionTimeout, entry.connectionError)
    }

    @Test
    fun `records cancelled attempt and preserves cancellation`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            service = FakeSshConnectionService(
                result = sshConnectedResult(),
                onConnect = {
                    throw CancellationException("Connection cancelled")
                },
            ),
        )

        try {
            useCase("server-1")
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }

        val entry = historyRepository.savedEntries.single()
        assertEquals(SshConnectionHistoryStatus.Cancelled, entry.status)
        assertEquals(null, entry.connectionError)
    }

    @Test
    fun `does not record host trust review outcome`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            hostTrustRepository = FakeSshHostTrustRepository(),
        )

        val outcome = useCase("server-1")

        assertTrue(outcome is SshConnectionAttemptOutcome.HostTrustDecisionRequired)
        assertTrue(historyRepository.savedEntries.isEmpty())
    }

    @Test
    fun `does not record unresolved target failure`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository()
        val useCase = createUseCase(
            historyRepository = historyRepository,
            resolver = FakeConnectionTargetResolver(ConnectionTargetResolution.NotFound),
        )

        val outcome = useCase("missing-server")

        val result = (outcome as SshConnectionAttemptOutcome.ConnectionResult).result
        val failure = result as SshConnectionResult.Failed
        assertEquals(SshConnectionError.TargetNotFound, failure.error)
        assertTrue(historyRepository.savedEntries.isEmpty())
    }

    @Test
    fun `history persistence failure does not replace connected result`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository(
            onSave = {
                throw IllegalStateException("History unavailable")
            },
        )
        val useCase = createUseCase(historyRepository = historyRepository)

        val outcome = useCase("server-1")

        val result = (outcome as SshConnectionAttemptOutcome.ConnectionResult).result
        assertTrue(result is SshConnectionResult.Connected)
        assertTrue(historyRepository.savedEntries.isEmpty())
    }

    @Test
    fun `history persistence failure does not mask connection cancellation`() = runBlocking {
        val historyRepository = FakeSshConnectionHistoryRepository(
            onSave = {
                throw IllegalStateException("History unavailable")
            },
        )
        val useCase = createUseCase(
            historyRepository = historyRepository,
            service = FakeSshConnectionService(
                result = sshConnectedResult(),
                onConnect = {
                    throw CancellationException("Connection cancelled")
                },
            ),
        )

        try {
            useCase("server-1")
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }

        assertTrue(historyRepository.savedEntries.isEmpty())
    }

    private fun createUseCase(
        historyRepository: FakeSshConnectionHistoryRepository,
        resolver: FakeConnectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
        service: FakeSshConnectionService = FakeSshConnectionService(sshConnectedResult()),
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(),
        ),
        timeoutMillis: Long = 1_000L,
        currentTimeMillisProvider: () -> Long = { 1_000L },
    ): SshConnectionAttemptUseCase {
        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(hostTrustRepository),
        )

        return SshConnectionAttemptUseCase(
            connectionTargetResolver = resolver,
            connectionService = service,
            hostKeyObservationService = FakeSshHostKeyObservationService(
                SshHostKeyObservationResult.Observed(observedHostKey()),
            ),
            hostTrustDecisionUseCase = hostTrustDecisionUseCase,
            connectionHistoryRepository = historyRepository,
            timeoutMillis = timeoutMillis,
            currentTimeMillisProvider = currentTimeMillisProvider,
            historyEntryIdProvider = { "history-1" },
        )
    }

    private fun sequentialClock(vararg values: Long): () -> Long {
        val iterator = values.iterator()
        return {
            check(iterator.hasNext()) {
                "No test clock value remains."
            }
            iterator.next()
        }
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

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 2222,
        )
    }

    private fun fingerprint(): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = "abc123",
        )
    }

    private fun observedHostKey(): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }

    private fun trustedHostKey(): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }
}
