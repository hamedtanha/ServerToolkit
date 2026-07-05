package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository

class FakeSshHostTrustRepository(
    initialTrustedHostKey: SshTrustedHostKey? = null,
) : SshHostTrustRepository {

    var trustedHostKey: SshTrustedHostKey? = initialTrustedHostKey
        private set

    var saveCallCount: Int = 0
        private set

    var removeCallCount: Int = 0
        private set

    override suspend fun getTrustedHostKey(
        endpoint: SshHostEndpoint,
    ): SshTrustedHostKey? {
        return trustedHostKey?.takeIf { trustedKey ->
            trustedKey.endpoint == endpoint
        }
    }

    override suspend fun saveTrustedHostKey(
        trustedHostKey: SshTrustedHostKey,
    ) {
        val existingTrustedHostKey = this.trustedHostKey

        check(
            existingTrustedHostKey == null ||
                existingTrustedHostKey.endpoint != trustedHostKey.endpoint,
        ) {
            "Trusted host key already exists for endpoint."
        }

        saveCallCount += 1
        this.trustedHostKey = trustedHostKey
    }

    override suspend fun removeTrustedHostKey(
        endpoint: SshHostEndpoint,
    ) {
        removeCallCount += 1

        if (trustedHostKey?.endpoint == endpoint) {
            trustedHostKey = null
        }
    }
}
