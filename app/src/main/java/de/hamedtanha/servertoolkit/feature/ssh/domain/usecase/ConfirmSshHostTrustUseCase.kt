package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import javax.inject.Inject

class ConfirmSshHostTrustUseCase @Inject constructor(
    private val hostTrustDecisionUseCase: SshHostTrustDecisionUseCase,
    private val hostTrustRepository: SshHostTrustRepository,
) {

    suspend operator fun invoke(
        observedHostKey: SshObservedHostKey,
    ): SshHostTrustDecision {
        return when (val decision = hostTrustDecisionUseCase(observedHostKey)) {
            is SshHostTrustDecision.ReviewRequired -> trustUnknownHostKey(decision)

            is SshHostTrustDecision.Accepted -> decision

            is SshHostTrustDecision.BlockedChangedHostKey -> decision
        }
    }

    private suspend fun trustUnknownHostKey(
        decision: SshHostTrustDecision.ReviewRequired,
    ): SshHostTrustDecision.Accepted {
        val trustedHostKey = SshTrustedHostKey(
            endpoint = decision.observedHostKey.endpoint,
            fingerprint = decision.observedHostKey.fingerprint,
        )

        hostTrustRepository.saveTrustedHostKey(trustedHostKey)

        return SshHostTrustDecision.Accepted(trustedHostKey)
    }
}
