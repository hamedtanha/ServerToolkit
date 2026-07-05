package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Describes the target and authentication input required to start an SSH connection attempt.
 *
 * Authentication input may temporarily contain sensitive values. Those values must not be logged,
 * persisted, saved in UI state, or exposed through string representations.
 */
data class SshConnectionRequest(
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
    val authenticationInput: SshAuthenticationInput = SshAuthenticationInput.None,
) {

    fun clearAuthenticationInput() {
        authenticationInput.clearSensitiveValues()
    }
}
