package de.hamedtanha.servertoolkit.feature.ssh.domain.model

sealed interface SshHostTrustStatus {

    data class Unknown(
        val observedHostKey: SshObservedHostKey,
    ) : SshHostTrustStatus

    data class Trusted(
        val trustedHostKey: SshTrustedHostKey,
    ) : SshHostTrustStatus

    data class Changed(
        val trustedHostKey: SshTrustedHostKey,
        val observedHostKey: SshObservedHostKey,
    ) : SshHostTrustStatus
}
