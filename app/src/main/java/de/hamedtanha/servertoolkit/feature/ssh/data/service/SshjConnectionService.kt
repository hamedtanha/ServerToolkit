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
 * SSHJ-backed trusted connection service shell.
 *
 * This service may open a short-lived SSHJ transport connection only after a trusted host key has
 * been resolved and installed as the SSHJ host-key verifier. Authentication, long-lived session
 * ownership, command execution, and terminal interaction remain disabled in this implementation
 * gate.
 */
class SshjConnectionService @Inject constructor(
    private val authenticationAdapter: SshjAuthenticationAdapter,
    private val hostTrustRepository: SshHostTrustRepository,
    trustedHostKeyVerifierFactory: SshjTrustedHostKeyVerifierFactory,
) : SshConnectionService {

    private var trustedConnectionExecutor: SshjTrustedConnectionExecutor =
        SshjNetworkTrustedConnectionExecutor(trustedHostKeyVerifierFactory)

    internal constructor(
        authenticationAdapter: SshjAuthenticationAdapter,
        hostTrustRepository: SshHostTrustRepository,
        trustedConnectionExecutor: SshjTrustedConnectionExecutor,
    ) : this(
        authenticationAdapter = authenticationAdapter,
        hostTrustRepository = hostTrustRepository,
        trustedHostKeyVerifierFactory = SshjTrustedHostKeyVerifierFactory(),
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
                    val result = trustedConnectionExecutor.connect(
                        request = request,
                        trustedHostKey = trustedHostKey,
                    )
                ) {
                    SshjTrustedConnectionExecutionResult.Connected -> {
                        SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration)
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
}

private fun SshConnectionRequest.toHostEndpoint(): SshHostEndpoint {
    return SshHostEndpoint(
        serverId = serverId,
        host = host,
        port = port,
    )
}
