package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * User-visible SSH host key fingerprint.
 *
 * The algorithm is kept separate from the fingerprint value so the UI can later present
 * fingerprints such as SHA256 values without parsing display text.
 */
data class SshHostKeyFingerprint(
    val algorithm: String,
    val value: String,
) {
    init {
        require(algorithm.isNotBlank()) {
            "Host key fingerprint algorithm must not be blank."
        }
        require(value.isNotBlank()) {
            "Host key fingerprint value must not be blank."
        }
    }
}
