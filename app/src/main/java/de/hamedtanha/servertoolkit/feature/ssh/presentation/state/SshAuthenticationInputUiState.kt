package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod

/**
 * UI-safe authentication input state.
 *
 * Secret values are intentionally excluded. The UI state only exposes authentication method
 * selection and presence flags so passwords and passphrases do not appear in StateFlow snapshots,
 * logs, or tests.
 */
data class SshAuthenticationInputUiState(
    val selectedMethod: SshAuthenticationMethod = SshAuthenticationMethod.PASSWORD,
    val hasPasswordInput: Boolean = false,
    val hasPrivateKeyPassphraseInput: Boolean = false,
) {

    val hasSensitiveInput: Boolean
        get() = hasPasswordInput || hasPrivateKeyPassphraseInput
}
