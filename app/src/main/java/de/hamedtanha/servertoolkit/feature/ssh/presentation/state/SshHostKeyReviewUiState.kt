package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey

data class SshHostKeyReviewUiState(
    val serverId: String,
    val host: String,
    val port: Int,
    val fingerprintAlgorithm: String,
    val fingerprintValue: String,
) {

    val displayFingerprint: String
        get() = "$fingerprintAlgorithm:$fingerprintValue"
}

internal fun SshObservedHostKey.toReviewUiState(): SshHostKeyReviewUiState {
    return SshHostKeyReviewUiState(
        serverId = endpoint.serverId,
        host = endpoint.host,
        port = endpoint.port,
        fingerprintAlgorithm = fingerprint.algorithm,
        fingerprintValue = fingerprint.value,
    )
}
