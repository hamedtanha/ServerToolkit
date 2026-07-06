package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshjSessionOwnerRegistryTest {

    @Test
    fun `returns not found when closing unknown session`() {
        val registry = SshjSessionOwnerRegistry()

        val result = registry.close(sshSessionHandle())

        assertEquals(SshSessionCloseResult.NotFound, result)
    }

    @Test
    fun `registers and closes session owner`() {
        val registry = SshjSessionOwnerRegistry()
        val handle = sshSessionHandle()
        var closed = false

        val registered = registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    closed = true
                },
            ),
        )

        assertTrue(registered)
        assertTrue(registry.contains(handle))

        val result = registry.close(handle)

        assertEquals(SshSessionCloseResult.Closed, result)
        assertTrue(closed)
        assertFalse(registry.contains(handle))
        assertEquals(SshSessionCloseResult.NotFound, registry.close(handle))
    }

    @Test
    fun `does not replace an existing session owner with the same session id`() {
        val registry = SshjSessionOwnerRegistry()
        val handle = sshSessionHandle()
        var firstOwnerClosed = false
        var secondOwnerClosed = false

        val firstRegistered = registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    firstOwnerClosed = true
                },
            ),
        )
        val secondRegistered = registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    secondOwnerClosed = true
                },
            ),
        )

        assertTrue(firstRegistered)
        assertFalse(secondRegistered)

        val result = registry.close(handle)

        assertEquals(SshSessionCloseResult.Closed, result)
        assertTrue(firstOwnerClosed)
        assertFalse(secondOwnerClosed)
    }

    @Test
    fun `keeps session owner registered when close action fails`() {
        val registry = SshjSessionOwnerRegistry()
        val handle = sshSessionHandle()
        var closeAttempts = 0

        registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    closeAttempts += 1
                    if (closeAttempts == 1) {
                        throw IllegalStateException("Simulated close failure")
                    }
                },
            ),
        )

        val failedResult = registry.close(handle)

        assertEquals(SshSessionCloseResult.Failed, failedResult)
        assertTrue(registry.contains(handle))

        val closedResult = registry.close(handle)

        assertEquals(SshSessionCloseResult.Closed, closedResult)
        assertEquals(2, closeAttempts)
        assertFalse(registry.contains(handle))
    }

    @Test
    fun `preserves cancellation when close action is cancelled`() {
        val registry = SshjSessionOwnerRegistry()
        val handle = sshSessionHandle()

        registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    throw CancellationException("cancelled")
                },
            ),
        )

        try {
            registry.close(handle)
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }

        assertTrue(registry.contains(handle))
    }
}
