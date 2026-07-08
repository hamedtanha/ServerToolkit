package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SSHJ-backed trusted connection service.
 *
 * This service opens an SSHJ transport connection only after a trusted host key has been resolved,
 * executes password authentication inside that trusted boundary, and registers the authenticated
 * SSHJ client in the data-layer session owner registry. Command execution remains
 * non-interactive and is routed through project-owned session handles. Terminal interaction remains
 * out of scope.
 */
class SshjConnectionService @Inject constructor(
    private val authenticationAdapter: SshjAuthenticationAdapter,
    private val hostTrustRepository: SshHostTrustRepository,
    private val sessionOwnerRegistry: SshjSessionOwnerRegistry,
    trustedHostKeyVerifierFactory: SshjTrustedHostKeyVerifierFactory,
    authenticationExecutor: SshjAuthenticationExecutor,
) : SshConnectionService {

    private var trustedConnectionExecutor: SshjTrustedConnectionExecutor =
        SshjNetworkTrustedConnectionExecutor(
            trustedHostKeyVerifierFactory = trustedHostKeyVerifierFactory,
            authenticationExecutor = authenticationExecutor,
        )

    internal constructor(
        authenticationAdapter: SshjAuthenticationAdapter,
        hostTrustRepository: SshHostTrustRepository,
        trustedConnectionExecutor: SshjTrustedConnectionExecutor,
        sessionOwnerRegistry: SshjSessionOwnerRegistry = SshjSessionOwnerRegistry(),
    ) : this(
        authenticationAdapter = authenticationAdapter,
        hostTrustRepository = hostTrustRepository,
        sessionOwnerRegistry = sessionOwnerRegistry,
        trustedHostKeyVerifierFactory = SshjTrustedHostKeyVerifierFactory(),
        authenticationExecutor = SshjAuthenticationExecutor(),
    ) {
        this.trustedConnectionExecutor = trustedConnectionExecutor
    }

    override suspend fun connect(request: SshConnectionRequest): SshConnectionResult {
        val authenticationMapping = authenticationAdapter.map(request)

        return try {
            withContext(Dispatchers.IO) {
                val trustedHostKey = hostTrustRepository.getTrustedHostKey(
                    request.toHostEndpoint(),
                ) ?: return@withContext SshConnectionResult.Failed(
                    SshConnectionError.HostTrustRequired,
                )

                when (
                    val result = trustedConnectionExecutor.connectAndAuthenticate(
                        request = request,
                        trustedHostKey = trustedHostKey,
                        authenticationMapping = authenticationMapping,
                    )
                ) {
                    is SshjTrustedConnectionExecutionResult.Connected -> {
                        registerConnectedSession(result.sessionOwner)
                    }

                    is SshjTrustedConnectionExecutionResult.Failed -> {
                        SshConnectionResult.Failed(result.error)
                    }
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshConnectionResult.Failed(SshConnectionError.Unknown)
        } finally {
            authenticationMapping.clearSensitiveValues()
        }
    }

    private fun registerConnectedSession(
        sessionOwner: SshjSessionOwner,
    ): SshConnectionResult {
        return if (sessionOwnerRegistry.register(sessionOwner)) {
            SshConnectionResult.Connected(sessionOwner.sessionHandle)
        } else {
            runCatching {
                sessionOwner.close()
            }
            SshConnectionResult.Failed(SshConnectionError.Unknown)
        }
    }
}

private fun SshConnectionRequest.toHostEndpoint(): SshHostEndpoint {
    return SshHostEndpoint(
        serverId = serverId,
        host = host,
        port = port,
    )
}
