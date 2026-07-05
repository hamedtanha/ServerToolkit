package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Supported SSH authentication input modes.
 *
 * This model intentionally contains no credential values.
 */
enum class SshAuthenticationMethod {
    PASSWORD,
    PRIVATE_KEY,
}
