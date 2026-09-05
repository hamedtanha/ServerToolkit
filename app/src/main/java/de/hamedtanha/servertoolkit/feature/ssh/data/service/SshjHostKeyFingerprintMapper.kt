package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import net.schmizz.sshj.common.Buffer
import net.schmizz.sshj.common.SecurityUtils

/**
 * Maps SSHJ public host keys into project-owned host-key fingerprints.
 *
 * Canonical fingerprints hash the SSH wire representation used by OpenSSH. Historical mappers are
 * retained only so previously persisted trust rows can verify the same observed key without being
 * silently rewritten or reinterpreted.
 */
internal fun PublicKey.toOpenSshSha256Fingerprint(): SshHostKeyFingerprint {
    val sshPublicKeyBytes = Buffer.PlainBuffer()
        .putPublicKey(this)
        .compactData

    return SshHostKeyFingerprint(
        algorithm = SHA256_DISPLAY_ALGORITHM,
        value = sha256Base64WithoutPadding(sshPublicKeyBytes),
        encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
    )
}

internal fun PublicKey.toLegacyJavaEncodedSha256Fingerprint(): SshHostKeyFingerprint {
    return SshHostKeyFingerprint(
        algorithm = SHA256_DISPLAY_ALGORITHM,
        value = sha256Base64WithoutPadding(encoded),
        encoding = SshHostKeyFingerprintEncoding.LegacyJavaPublicKey,
    )
}

internal fun PublicKey.toLegacySshjMd5Fingerprint(): SshHostKeyFingerprint {
    return SshHostKeyFingerprint(
        algorithm = MD5_DISPLAY_ALGORITHM,
        value = SecurityUtils.getFingerprint(this),
        encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
    )
}

private fun sha256Base64WithoutPadding(bytes: ByteArray): String {
    val digest = MessageDigest
        .getInstance(SHA256_DIGEST_ALGORITHM)
        .digest(bytes)

    return Base64
        .getEncoder()
        .withoutPadding()
        .encodeToString(digest)
}

private const val SHA256_DISPLAY_ALGORITHM = "SHA256"
private const val SHA256_DIGEST_ALGORITHM = "SHA-256"
private const val MD5_DISPLAY_ALGORITHM = "MD5"
