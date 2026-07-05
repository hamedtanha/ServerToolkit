package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class SshSessionCloseResultTest {

    @Test
    fun `closed result represents successful session close`() {
        val result: SshSessionCloseResult = SshSessionCloseResult.Closed

        assertTrue(result is SshSessionCloseResult.Closed)
    }

    @Test
    fun `not found result represents missing session owner`() {
        val result: SshSessionCloseResult = SshSessionCloseResult.NotFound

        assertTrue(result is SshSessionCloseResult.NotFound)
    }

    @Test
    fun `failed result represents unsuccessful session close`() {
        val result: SshSessionCloseResult = SshSessionCloseResult.Failed

        assertTrue(result is SshSessionCloseResult.Failed)
    }
}
