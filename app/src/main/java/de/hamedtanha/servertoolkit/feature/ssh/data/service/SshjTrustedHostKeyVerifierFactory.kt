package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
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

        return key.matches(trustedHostKey.fingerprint)
    }

    override fun findExistingAlgorithms(
        hostname: String,
        port: Int,
    ): MutableList<String> {
        return mutableListOf()
    }
}

private fun PublicKey.matches(trustedFingerprint: SshHostKeyFingerprint): Boolean {
    val observedFingerprint = when {
        trustedFingerprint.algorithm == SHA256_DISPLAY_ALGORITHM &&
            trustedFingerprint.encoding == SshHostKeyFingerprintEncoding.OpenSshWire ->
            toOpenSshSha256Fingerprint()

        trustedFingerprint.algorithm == SHA256_DISPLAY_ALGORITHM &&
            trustedFingerprint.encoding == SshHostKeyFingerprintEncoding.LegacyJavaPublicKey ->
            toLegacyJavaEncodedSha256Fingerprint()

        trustedFingerprint.algorithm == MD5_DISPLAY_ALGORITHM &&
            trustedFingerprint.encoding == SshHostKeyFingerprintEncoding.OpenSshWire ->
            toLegacySshjMd5Fingerprint()

        else -> return false
    }

    return observedFingerprint == trustedFingerprint
}

private const val SHA256_DISPLAY_ALGORITHM = "SHA256"
private const val MD5_DISPLAY_ALGORITHM = "MD5"
