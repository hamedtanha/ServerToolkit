package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey

internal fun SshTrustedHostKey.toEntity(): SshTrustedHostKeyEntity {
    return SshTrustedHostKeyEntity(
        serverId = endpoint.serverId,
        host = endpoint.host,
        port = endpoint.port,
        fingerprintAlgorithm = fingerprint.algorithm,
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
        fingerprint = SshHostKeyFingerprint(
            algorithm = fingerprintAlgorithm,
            value = fingerprintValue,
        ),
    )
}
