package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64

/**
 * Maps SSHJ public host keys into project-owned host-key fingerprints.
 *
 * This mapper keeps host-key fingerprint generation in the data layer so domain and presentation
 * models do not depend on SSHJ APIs.
 */
internal fun PublicKey.toSshjHostKeyFingerprint(): SshHostKeyFingerprint {
    val digest = MessageDigest
        .getInstance(HOST_KEY_FINGERPRINT_DIGEST_ALGORITHM)
        .digest(encoded)

    return SshHostKeyFingerprint(
        algorithm = HOST_KEY_FINGERPRINT_DISPLAY_ALGORITHM,
        value = Base64
            .getEncoder()
            .withoutPadding()
            .encodeToString(digest),
    )
}

private const val HOST_KEY_FINGERPRINT_DISPLAY_ALGORITHM = "SHA256"
private const val HOST_KEY_FINGERPRINT_DIGEST_ALGORITHM = "SHA-256"
