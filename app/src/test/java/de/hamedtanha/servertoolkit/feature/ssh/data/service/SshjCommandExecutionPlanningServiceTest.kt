package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionPlanningResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjCommandExecutionPlanningServiceTest {

    @Test
    fun `plans command execution when session owner exists`() = runBlocking {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        var ownerClosed = false
        registry.register(
            SshjSessionOwner(
                sessionHandle = sessionHandle,
                closeAction = {
                    ownerClosed = true
                },
            ),
        )
        val service = SshjCommandExecutionPlanningService(registry)

        val result = service.plan(
            SshCommandRequest(
                sessionHandle = sessionHandle,
                command = "uptime",
                timeoutMillis = 5_000,
            ),
        )

        assertTrue(result is SshCommandExecutionPlanningResult.Planned)
        val planned = result as SshCommandExecutionPlanningResult.Planned
        assertEquals(sessionHandle, planned.plan.sessionHandle)
        assertEquals("uptime", planned.plan.command)
        assertEquals(5_000, planned.plan.timeoutMillis)
        assertFalse(ownerClosed)
    }

    @Test
    fun `fails planning when session owner does not exist`() = runBlocking {
        val service = SshjCommandExecutionPlanningService(SshjSessionOwnerRegistry())

        val result = service.plan(
            SshCommandRequest(
                sessionHandle = sshSessionHandle(),
                command = "uptime",
            ),
        )

        assertEquals(
            SshCommandExecutionPlanningResult.Failed(SshCommandExecutionError.SessionNotFound),
            result,
        )
    }
}
