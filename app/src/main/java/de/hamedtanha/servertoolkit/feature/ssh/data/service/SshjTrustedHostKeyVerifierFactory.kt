package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.security.PublicKey
import javax.inject.Inject
import net.schmizz.sshj.transport.verification.HostKeyVerifier

/**
 * Creates SSHJ host-key verifiers from project-owned trusted host-key records.
 *
 * This boundary is intentionally data-layer only. It does not open network connections and does not
 * decide trust. It only enforces a previously accepted trusted host key during a future SSHJ
 * connection attempt.
 */
class SshjTrustedHostKeyVerifierFactory @Inject constructor() {

    internal fun create(trustedHostKey: SshTrustedHostKey): HostKeyVerifier {
        return SshjTrustedHostKeyVerifier(trustedHostKey)
    }
}

private class SshjTrustedHostKeyVerifier(
    private val trustedHostKey: SshTrustedHostKey,
) : HostKeyVerifier {

    override fun verify(
        hostname: String,
        port: Int,
        key: PublicKey,
    ): Boolean {
        val trustedEndpoint = trustedHostKey.endpoint
        if (hostname != trustedEndpoint.host || port != trustedEndpoint.port) {
            return false
        }

        return key.toSshjHostKeyFingerprint() == trustedHostKey.fingerprint
    }

    override fun findExistingAlgorithms(
        hostname: String,
        port: Int,
    ): MutableList<String> {
        return mutableListOf()
    }
}
