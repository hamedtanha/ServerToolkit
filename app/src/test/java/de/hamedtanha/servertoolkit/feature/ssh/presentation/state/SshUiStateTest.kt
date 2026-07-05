package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SshUiStateTest {

    @Test
    fun `uses not started status by default`() {
        val uiState = SshUiState()

        assertEquals(SshConnectionStatus.NotStarted, uiState.status)
        assertEquals("Not connected", uiState.statusLabel)
        assertEquals("SSH connectivity is not implemented yet.", uiState.message)
        assertNull(uiState.hostKeyReview)
        assertFalse(uiState.isHostKeyReviewRequired)
        assertEquals(SshAuthenticationMethod.PASSWORD, uiState.authenticationInput.selectedMethod)
        assertFalse(uiState.authenticationInput.hasSensitiveInput)
    }
}
