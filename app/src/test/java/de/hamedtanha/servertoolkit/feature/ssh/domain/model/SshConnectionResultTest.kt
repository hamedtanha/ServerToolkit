package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshConnectionResultTest {

    @Test
    fun `connected result carries project-owned session handle`() {
        val sessionHandle = sshSessionHandle()
        val result: SshConnectionResult = SshConnectionResult.Connected(sessionHandle)

        assertTrue(result is SshConnectionResult.Connected)
        assertEquals(sessionHandle, (result as SshConnectionResult.Connected).sessionHandle)
    }

    @Test
    fun `failed result carries domain error category`() {
        val result = SshConnectionResult.Failed(
            error = SshConnectionError.ConnectionTimeout,
        )

        assertEquals(SshConnectionError.ConnectionTimeout, result.error)
    }
}
