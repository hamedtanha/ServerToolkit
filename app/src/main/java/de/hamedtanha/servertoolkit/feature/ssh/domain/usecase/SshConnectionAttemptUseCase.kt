package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class SshConnectionAttemptUseCase @Inject constructor(
    private val connectionTargetResolver: ConnectionTargetResolver,
    private val connectionService: SshConnectionService,
) {

    private var timeoutMillis: Long = DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS

    internal constructor(
        connectionTargetResolver: ConnectionTargetResolver,
        connectionService: SshConnectionService,
        timeoutMillis: Long,
    ) : this(
        connectionTargetResolver = connectionTargetResolver,
        connectionService = connectionService,
    ) {
        this.timeoutMillis = timeoutMillis
    }

    suspend operator fun invoke(
        serverId: String,
        authenticationInput: SshAuthenticationInput = SshAuthenticationInput.None,
    ): SshConnectionResult {
        return try {
            withTimeout(timeoutMillis) {
                executeConnectionAttempt(
                    serverId = serverId,
                    authenticationInput = authenticationInput,
                )
            }
        } catch (error: TimeoutCancellationException) {
            SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshConnectionResult.Failed(SshConnectionError.Unknown)
        } finally {
            authenticationInput.clearSensitiveValues()
        }
    }

    private suspend fun executeConnectionAttempt(
        serverId: String,
        authenticationInput: SshAuthenticationInput,
    ): SshConnectionResult {
        return when (val resolution = connectionTargetResolver.resolve(serverId)) {
            is ConnectionTargetResolution.Resolved -> connectToResolvedTarget(
                target = resolution.target,
                authenticationInput = authenticationInput,
            )

            ConnectionTargetResolution.NotFound -> SshConnectionResult.Failed(
                SshConnectionError.TargetNotFound,
            )

            is ConnectionTargetResolution.Invalid -> SshConnectionResult.Failed(
                SshConnectionError.MissingConnectionMetadata,
            )
        }
    }

    private suspend fun connectToResolvedTarget(
        target: RemoteConnectionTarget,
        authenticationInput: SshAuthenticationInput,
    ): SshConnectionResult {
        return connectionService.connect(
            SshConnectionRequest(
                serverId = target.serverId,
                host = target.host,
                port = target.port,
                username = target.username,
                authenticationInput = authenticationInput,
            ),
        )
    }

    private companion object {
        const val DEFAULT_CONNECTION_ATTEMPT_TIMEOUT_MILLIS = 10_000L
    }
}
