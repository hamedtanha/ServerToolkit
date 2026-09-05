package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SshConnectionAttemptUseCase @Inject constructor(
    private val connectionTargetResolver: ConnectionTargetResolver,
    private val connectionService: SshConnectionService,
    private val hostKeyObservationService: SshHostKeyObservationService,
    private val hostTrustDecisionUseCase: SshHostTrustDecisionUseCase,
    private val connectionHistoryRepository: SshConnectionHistoryRepository,
) {

    private var timeoutMillis: Long = DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS

    private var currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() }

    private var historyEntryIdProvider: () -> String = { UUID.randomUUID().toString() }

    internal constructor(
        connectionTargetResolver: ConnectionTargetResolver,
        connectionService: SshConnectionService,
        hostKeyObservationService: SshHostKeyObservationService,
        hostTrustDecisionUseCase: SshHostTrustDecisionUseCase,
        connectionHistoryRepository: SshConnectionHistoryRepository,
        timeoutMillis: Long,
        currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() },
        historyEntryIdProvider: () -> String = { UUID.randomUUID().toString() },
    ) : this(
        connectionTargetResolver = connectionTargetResolver,
        connectionService = connectionService,
        hostKeyObservationService = hostKeyObservationService,
        hostTrustDecisionUseCase = hostTrustDecisionUseCase,
        connectionHistoryRepository = connectionHistoryRepository,
    ) {
        this.timeoutMillis = timeoutMillis
        this.currentTimeMillisProvider = currentTimeMillisProvider
        this.historyEntryIdProvider = historyEntryIdProvider
    }

    suspend operator fun invoke(
        serverId: String,
        authenticationInput: SshAuthenticationInput = SshAuthenticationInput.None,
    ): SshConnectionAttemptOutcome {
        var attemptedAtEpochMillis = 0L
        var resolvedTarget: RemoteConnectionTarget? = null
        var pendingSessionHandle: SshSessionHandle? = null

        fun discardPendingConnectedSession() {
            val sessionHandle = pendingSessionHandle ?: return
            pendingSessionHandle = null

            try {
                connectionService.discardUndeliveredSession(sessionHandle)
            } catch (_: Exception) {
                // Rollback failure must not replace the primary cancellation, timeout, or failure.
            }
        }

        return try {
            attemptedAtEpochMillis = currentTimeMillisProvider()

            val outcome = withTimeout(timeoutMillis) {
                executeConnectionAttempt(
                    serverId = serverId,
                    authenticationInput = authenticationInput,
                    onTargetResolved = { target -> resolvedTarget = target },
                    onConnectedSession = { handle -> pendingSessionHandle = handle },
                )
            }

            recordConnectionOutcome(
                target = resolvedTarget,
                outcome = outcome,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
            )
            pendingSessionHandle = null
            outcome
        } catch (error: TimeoutCancellationException) {
            discardPendingConnectedSession()
            val outcome = SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout),
            )
            recordConnectionOutcome(
                target = resolvedTarget,
                outcome = outcome,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
            )
            outcome
        } catch (error: CancellationException) {
            discardPendingConnectedSession()
            recordCancelledConnectionAttempt(
                target = resolvedTarget,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
            )
            throw error
        } catch (error: Exception) {
            discardPendingConnectedSession()
            val outcome = SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.Unknown),
            )
            recordConnectionOutcome(
                target = resolvedTarget,
                outcome = outcome,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
            )
            outcome
        } finally {
            authenticationInput.clearSensitiveValues()
        }
    }

    private suspend fun executeConnectionAttempt(
        serverId: String,
        authenticationInput: SshAuthenticationInput,
        onTargetResolved: (RemoteConnectionTarget) -> Unit,
        onConnectedSession: (SshSessionHandle) -> Unit,
    ): SshConnectionAttemptOutcome {
        return when (val resolution = connectionTargetResolver.resolve(serverId)) {
            is ConnectionTargetResolution.Resolved -> {
                onTargetResolved(resolution.target)
                connectToResolvedTarget(
                    target = resolution.target,
                    authenticationInput = authenticationInput,
                    onConnectedSession = onConnectedSession,
                )
            }

            ConnectionTargetResolution.NotFound -> SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.TargetNotFound),
            )

            is ConnectionTargetResolution.Invalid -> SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.MissingConnectionMetadata),
            )
        }
    }

    private suspend fun connectToResolvedTarget(
        target: RemoteConnectionTarget,
        authenticationInput: SshAuthenticationInput,
        onConnectedSession: (SshSessionHandle) -> Unit,
    ): SshConnectionAttemptOutcome {
        val request = SshConnectionRequest(
            serverId = target.serverId,
            host = target.host,
            port = target.port,
            username = target.username,
            authenticationInput = authenticationInput,
        )

        return when (val observation = hostKeyObservationService.observeHostKey(request)) {
            is SshHostKeyObservationResult.Observed -> handleObservedHostKey(
                request = request,
                observation = observation,
                onConnectedSession = onConnectedSession,
            )

            SshHostKeyObservationResult.Unavailable -> SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            )
        }
    }

    private suspend fun handleObservedHostKey(
        request: SshConnectionRequest,
        observation: SshHostKeyObservationResult.Observed,
        onConnectedSession: (SshSessionHandle) -> Unit,
    ): SshConnectionAttemptOutcome {
        return when (val decision = hostTrustDecisionUseCase(observation.observedHostKey)) {
            is SshHostTrustDecision.Accepted -> {
                val connectionResult = connectionService.connect(request)
                if (connectionResult is SshConnectionResult.Connected) {
                    onConnectedSession(connectionResult.sessionHandle)
                    currentCoroutineContext().ensureActive()
                }
                SshConnectionAttemptOutcome.ConnectionResult(connectionResult)
            }

            is SshHostTrustDecision.ReviewRequired -> {
                request.clearAuthenticationInput()
                SshConnectionAttemptOutcome.HostTrustDecisionRequired(decision)
            }

            is SshHostTrustDecision.BlockedChangedHostKey -> {
                request.clearAuthenticationInput()
                SshConnectionAttemptOutcome.HostTrustDecisionRequired(decision)
            }
        }
    }

    private suspend fun recordConnectionOutcome(
        target: RemoteConnectionTarget?,
        outcome: SshConnectionAttemptOutcome,
        attemptedAtEpochMillis: Long,
    ) {
        val connectionResult =
            (outcome as? SshConnectionAttemptOutcome.ConnectionResult)?.result ?: return
        val resolvedTarget = target ?: return

        when (connectionResult) {
            is SshConnectionResult.Connected -> recordConnectionHistoryEntry(
                target = resolvedTarget,
                status = SshConnectionHistoryStatus.Connected,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
            )

            is SshConnectionResult.Failed -> recordConnectionHistoryEntry(
                target = resolvedTarget,
                status = SshConnectionHistoryStatus.Failed,
                attemptedAtEpochMillis = attemptedAtEpochMillis,
                connectionError = connectionResult.error,
            )
        }
    }

    private suspend fun recordCancelledConnectionAttempt(
        target: RemoteConnectionTarget?,
        attemptedAtEpochMillis: Long,
    ) {
        val resolvedTarget = target ?: return

        recordConnectionHistoryEntry(
            target = resolvedTarget,
            status = SshConnectionHistoryStatus.Cancelled,
            attemptedAtEpochMillis = attemptedAtEpochMillis,
        )
    }

    private suspend fun recordConnectionHistoryEntry(
        target: RemoteConnectionTarget,
        status: SshConnectionHistoryStatus,
        attemptedAtEpochMillis: Long,
        connectionError: SshConnectionError? = null,
    ) {
        withContext(NonCancellable) {
            try {
                connectionHistoryRepository.saveConnectionHistoryEntry(
                    SshConnectionHistoryEntry(
                        id = historyEntryIdProvider(),
                        serverId = target.serverId,
                        host = target.host,
                        port = target.port,
                        username = target.username,
                        status = status,
                        attemptedAtEpochMillis = attemptedAtEpochMillis,
                        completedAtEpochMillis = currentTimeMillisProvider()
                            .coerceAtLeast(attemptedAtEpochMillis),
                        connectionError = connectionError,
                    ),
                )
            } catch (_: Exception) {
                // History persistence must not replace the primary connection outcome.
            }
        }
    }

    private companion object {
        const val DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS = 10_000L
    }
}
