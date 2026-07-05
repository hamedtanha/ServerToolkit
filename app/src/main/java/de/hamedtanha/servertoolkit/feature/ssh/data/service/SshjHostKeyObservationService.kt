package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import java.security.PublicKey
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.verification.HostKeyVerifier

/**
 * SSHJ-backed host-key observation adapter.
 *
 * This adapter observes the remote host key before authentication. The verifier intentionally
 * rejects the observed key so SSHJ does not continue into authentication or long-lived session
 * ownership in this implementation gate.
 */
class SshjHostKeyObservationService @Inject constructor() : SshHostKeyObservationService {

    private var hostKeyObserver: SshjHostKeyObserver = SshjNetworkHostKeyObserver()

    internal constructor(
        hostKeyObserver: SshjHostKeyObserver,
    ) : this() {
        this.hostKeyObserver = hostKeyObserver
    }

    override suspend fun observeHostKey(request: SshConnectionRequest): SshHostKeyObservationResult {
        return try {
            withContext(Dispatchers.IO) {
                hostKeyObserver.observeHostKey(request)
                    ?.toObservedHostKey(request)
                    ?.let(SshHostKeyObservationResult::Observed)
                    ?: SshHostKeyObservationResult.Unavailable
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshHostKeyObservationResult.Unavailable
        }
    }
}

internal fun interface SshjHostKeyObserver {

    fun observeHostKey(request: SshConnectionRequest): PublicKey?
}

private class SshjNetworkHostKeyObserver : SshjHostKeyObserver {

    override fun observeHostKey(request: SshConnectionRequest): PublicKey? {
        var observedHostKey: PublicKey? = null

        try {
            SSHClient().use { client ->
                client.addHostKeyVerifier(
                    object : HostKeyVerifier {

                        override fun verify(
                            hostname: String,
                            port: Int,
                            key: PublicKey,
                        ): Boolean {
                            observedHostKey = key
                            return false
                        }

                        override fun findExistingAlgorithms(
                            hostname: String,
                            port: Int,
                        ): MutableList<String> {
                            return mutableListOf()
                        }
                    },
                )

                client.connect(request.host, request.port)
            }
        } catch (error: Exception) {
            // A rejected host-key verifier is expected to stop the SSH handshake after observation.
        }

        return observedHostKey
    }
}

private fun PublicKey.toObservedHostKey(request: SshConnectionRequest): SshObservedHostKey {
    return SshObservedHostKey(
        endpoint = SshHostEndpoint(
            serverId = request.serverId,
            host = request.host,
            port = request.port,
        ),
        fingerprint = SshHostKeyFingerprint(
            algorithm = SSHJ_FINGERPRINT_ALGORITHM,
            value = SecurityUtils.getFingerprint(this),
        ),
    )
}

private const val SSHJ_FINGERPRINT_ALGORITHM = "MD5"
