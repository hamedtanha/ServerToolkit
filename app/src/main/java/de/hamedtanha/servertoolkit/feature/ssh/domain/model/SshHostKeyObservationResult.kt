package de.hamedtanha.servertoolkit.feature.ssh.domain.model

sealed interface SshHostKeyObservationResult {

    data class Observed(
        val observedHostKey: SshObservedHostKey,
    ) : SshHostKeyObservationResult

    data object Unavailable : SshHostKeyObservationResult
}
