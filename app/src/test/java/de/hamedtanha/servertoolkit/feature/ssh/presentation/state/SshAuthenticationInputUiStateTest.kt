package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshAuthenticationInputUiStateTest {

    @Test
    fun `uses password method without sensitive input by default`() {
        val state = SshAuthenticationInputUiState()

        assertEquals(SshAuthenticationMethod.PASSWORD, state.selectedMethod)
        assertEquals("", state.username)
        assertFalse(state.hasPasswordInput)
        assertFalse(state.hasPrivateKeyPassphraseInput)
        assertFalse(state.hasSensitiveInput)
    }

    @Test
    fun `reports sensitive input when password is present`() {
        val state = SshAuthenticationInputUiState(
            hasPasswordInput = true,
        )

        assertTrue(state.hasSensitiveInput)
    }

    @Test
    fun `reports sensitive input when private key passphrase is present`() {
        val state = SshAuthenticationInputUiState(
            selectedMethod = SshAuthenticationMethod.PRIVATE_KEY,
            hasPrivateKeyPassphraseInput = true,
        )

        assertTrue(state.hasSensitiveInput)
    }
}
