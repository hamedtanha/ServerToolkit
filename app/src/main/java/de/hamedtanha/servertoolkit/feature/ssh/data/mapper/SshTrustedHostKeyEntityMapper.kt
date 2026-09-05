package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey

internal fun SshTrustedHostKey.toEntity(): SshTrustedHostKeyEntity {
    return SshTrustedHostKeyEntity(
        serverId = endpoint.serverId,
        host = endpoint.host,
        port = endpoint.port,
        fingerprintAlgorithm = fingerprint.toPersistedAlgorithm(),
        fingerprintValue = fingerprint.value,
    )
}

internal fun SshTrustedHostKeyEntity.toDomain(): SshTrustedHostKey {
    return SshTrustedHostKey(
        endpoint = SshHostEndpoint(
            serverId = serverId,
            host = host,
            port = port,
        ),
        fingerprint = fingerprintFromPersistence(
            persistedAlgorithm = fingerprintAlgorithm,
            value = fingerprintValue,
        ),
    )
}

private fun SshHostKeyFingerprint.toPersistedAlgorithm(): String {
    return when {
        algorithm == SHA256_DISPLAY_ALGORITHM &&
            encoding == SshHostKeyFingerprintEncoding.OpenSshWire ->
            PERSISTED_OPENSSH_SHA256_V1

        algorithm == SHA256_DISPLAY_ALGORITHM &&
            encoding == SshHostKeyFingerprintEncoding.LegacyJavaPublicKey ->
            PERSISTED_LEGACY_JAVA_SHA256

        algorithm == MD5_DISPLAY_ALGORITHM &&
            encoding == SshHostKeyFingerprintEncoding.OpenSshWire ->
            PERSISTED_LEGACY_SSHJ_MD5

        else -> throw IllegalArgumentException(
            "Unsupported SSH host key fingerprint persistence scheme.",
        )
    }
}

private fun fingerprintFromPersistence(
    persistedAlgorithm: String,
    value: String,
): SshHostKeyFingerprint {
    return when (persistedAlgorithm) {
        PERSISTED_OPENSSH_SHA256_V1 -> SshHostKeyFingerprint(
            algorithm = SHA256_DISPLAY_ALGORITHM,
            value = value,
            encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
        )

        PERSISTED_LEGACY_JAVA_SHA256 -> SshHostKeyFingerprint(
            algorithm = SHA256_DISPLAY_ALGORITHM,
            value = value,
            encoding = SshHostKeyFingerprintEncoding.LegacyJavaPublicKey,
        )

        PERSISTED_LEGACY_SSHJ_MD5 -> SshHostKeyFingerprint(
            algorithm = MD5_DISPLAY_ALGORITHM,
            value = value,
            encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
        )

        else -> throw IllegalArgumentException(
            "Unsupported persisted SSH host key fingerprint scheme: $persistedAlgorithm",
        )
    }
}

internal const val PERSISTED_OPENSSH_SHA256_V1 = "OPENSSH_SHA256_V1"
private const val PERSISTED_LEGACY_JAVA_SHA256 = "SHA256"
private const val PERSISTED_LEGACY_SSHJ_MD5 = "MD5"
private const val SHA256_DISPLAY_ALGORITHM = "SHA256"
private const val MD5_DISPLAY_ALGORITHM = "MD5"
