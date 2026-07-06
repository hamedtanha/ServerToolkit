package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import java.security.PublicKey
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.transport.verification.HostKeyVerifier

/**
 * SSHJ-backed host-key observation adapter.
 *
 * This adapter observes the remote host key before authentication. The verifier allows the
 * transport handshake to continue only long enough to capture the public host key. No
 * authentication is performed and the SSHJ client is closed immediately after observation.
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

private class SshjNetworkHostKeyObserver(
    private val clientFactory: SshjClientFactory = SshjClientFactory(),
) : SshjHostKeyObserver {

    override fun observeHostKey(request: SshConnectionRequest): PublicKey? {
        var observedHostKey: PublicKey? = null

        try {
            clientFactory.createClient().use { client ->
                client.connectTimeout = SSHJ_HOST_KEY_OBSERVATION_TIMEOUT_MILLIS
                client.timeout = SSHJ_HOST_KEY_OBSERVATION_TIMEOUT_MILLIS
                client.addHostKeyVerifier(
                    object : HostKeyVerifier {

                        override fun verify(
                            hostname: String,
                            port: Int,
                            key: PublicKey,
                        ): Boolean {
                            observedHostKey = key
                            return true
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
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Observation failures are contained so connection attempts can surface stable errors.
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
        fingerprint = toSshjHostKeyFingerprint(),
    )
}

private const val SSHJ_HOST_KEY_OBSERVATION_TIMEOUT_MILLIS = 10_000
