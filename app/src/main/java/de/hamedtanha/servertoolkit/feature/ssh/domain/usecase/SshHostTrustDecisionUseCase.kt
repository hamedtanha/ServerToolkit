package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import javax.inject.Inject

class SshHostTrustDecisionUseCase @Inject constructor(
    private val hostTrustEvaluator: SshHostTrustEvaluator,
) {

    suspend operator fun invoke(
        observedHostKey: SshObservedHostKey,
    ): SshHostTrustDecision {
        return when (val status = hostTrustEvaluator.evaluate(observedHostKey)) {
            is SshHostTrustStatus.Unknown -> SshHostTrustDecision.ReviewRequired(
                observedHostKey = status.observedHostKey,
            )

            is SshHostTrustStatus.Trusted -> SshHostTrustDecision.Accepted(
                trustedHostKey = status.trustedHostKey,
            )

            is SshHostTrustStatus.Changed -> SshHostTrustDecision.BlockedChangedHostKey(
                trustedHostKey = status.trustedHostKey,
                observedHostKey = status.observedHostKey,
            )
        }
    }
}
