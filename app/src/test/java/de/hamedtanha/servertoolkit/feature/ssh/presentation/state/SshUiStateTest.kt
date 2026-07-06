package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SshUiStateTest {

    @Test
    fun `uses not started status by default`() {
        val uiState = SshUiState()

        assertEquals(SshConnectionStatus.NotStarted, uiState.status)
        assertEquals("Not connected", uiState.statusLabel)
        assertEquals("Ready to start an SSH connection attempt.", uiState.message)
        assertNull(uiState.hostKeyReview)
        assertFalse(uiState.isHostKeyReviewRequired)
        assertEquals(SshAuthenticationMethod.PASSWORD, uiState.authenticationInput.selectedMethod)
        assertFalse(uiState.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `allows starting connection only before active connection exists`() {
        assertTrue(SshUiState(status = SshConnectionStatus.NotStarted).canStartConnection)
        assertFalse(SshUiState(status = SshConnectionStatus.Connecting).canStartConnection)
        assertFalse(SshUiState(status = SshConnectionStatus.Connected).canStartConnection)
        assertTrue(SshUiState(status = SshConnectionStatus.Failed).canStartConnection)
    }
}
