package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import javax.inject.Inject

class SshHostTrustEvaluator @Inject constructor(
    private val hostTrustRepository: SshHostTrustRepository,
) {

    suspend fun evaluate(observedHostKey: SshObservedHostKey): SshHostTrustStatus {
        val trustedHostKey = hostTrustRepository.getTrustedHostKey(
            endpoint = observedHostKey.endpoint,
        ) ?: return SshHostTrustStatus.Unknown(observedHostKey)

        return if (trustedHostKey.fingerprint == observedHostKey.fingerprint) {
            SshHostTrustStatus.Trusted(trustedHostKey)
        } else {
            SshHostTrustStatus.Changed(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            )
        }
    }
}
