package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshConnectionResultTest {

    @Test
    fun `connected result is represented as success state`() {
        val result: SshConnectionResult = SshConnectionResult.Connected

        assertTrue(result is SshConnectionResult.Connected)
    }

    @Test
    fun `failed result carries domain error category`() {
        val result = SshConnectionResult.Failed(
            error = SshConnectionError.ConnectionTimeout,
        )

        assertEquals(SshConnectionError.ConnectionTimeout, result.error)
    }
}
