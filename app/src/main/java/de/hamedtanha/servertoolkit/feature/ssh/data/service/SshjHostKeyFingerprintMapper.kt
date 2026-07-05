package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import java.security.PublicKey
import net.schmizz.sshj.common.SecurityUtils

/**
 * Maps SSHJ public host keys into project-owned host-key fingerprints.
 *
 * This mapper keeps the SSHJ-specific fingerprint algorithm in the data layer so domain and
 * presentation models do not depend on SSHJ APIs.
 */
internal fun PublicKey.toSshjHostKeyFingerprint(): SshHostKeyFingerprint {
    return SshHostKeyFingerprint(
        algorithm = SSHJ_FINGERPRINT_ALGORITHM,
        value = SecurityUtils.getFingerprint(this),
    )
}

private const val SSHJ_FINGERPRINT_ALGORITHM = "MD5"
