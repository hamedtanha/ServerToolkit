package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class SshConnectionAttemptUseCase @Inject constructor(
    private val connectionTargetResolver: ConnectionTargetResolver,
    private val connectionService: SshConnectionService,
    private val hostKeyObservationService: SshHostKeyObservationService,
    private val hostTrustDecisionUseCase: SshHostTrustDecisionUseCase,
) {

    private var timeoutMillis: Long = DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS

    internal constructor(
        connectionTargetResolver: ConnectionTargetResolver,
        connectionService: SshConnectionService,
        hostKeyObservationService: SshHostKeyObservationService,
        hostTrustDecisionUseCase: SshHostTrustDecisionUseCase,
        timeoutMillis: Long,
    ) : this(
        connectionTargetResolver = connectionTargetResolver,
        connectionService = connectionService,
        hostKeyObservationService = hostKeyObservationService,
        hostTrustDecisionUseCase = hostTrustDecisionUseCase,
    ) {
        this.timeoutMillis = timeoutMillis
    }

    suspend operator fun invoke(
        serverId: String,
        authenticationInput: SshAuthenticationInput = SshAuthenticationInput.None,
    ): SshConnectionAttemptOutcome {
        return try {
            withTimeout(timeoutMillis) {
                executeConnectionAttempt(
                    serverId = serverId,
                    authenticationInput = authenticationInput,
                )
            }
        } catch (error: TimeoutCancellationException) {
            SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.Unknown),
            )
        } finally {
            authenticationInput.clearSensitiveValues()
        }
    }

    private suspend fun executeConnectionAttempt(
        serverId: String,
        authenticationInput: SshAuthenticationInput,
    ): SshConnectionAttemptOutcome {
        return when (val resolution = connectionTargetResolver.resolve(serverId)) {
            is ConnectionTargetResolution.Resolved -> connectToResolvedTarget(
                target = resolution.target,
                authenticationInput = authenticationInput,
            )

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
            )

            SshHostKeyObservationResult.Unavailable -> SshConnectionAttemptOutcome.ConnectionResult(
                SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            )
        }
    }

    private suspend fun handleObservedHostKey(
        request: SshConnectionRequest,
        observation: SshHostKeyObservationResult.Observed,
    ): SshConnectionAttemptOutcome {
        return when (val decision = hostTrustDecisionUseCase(observation.observedHostKey)) {
            is SshHostTrustDecision.Accepted -> SshConnectionAttemptOutcome.ConnectionResult(
                connectionService.connect(request),
            )

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

    private companion object {
        const val DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS = 10_000L
    }
}
