package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjCommandChannelExecutorTest {

    @Test
    fun `executes command and closes channel after successful completion`() {
        val channel = FakeCommandChannel(
            stdoutText = "ok",
            stderrText = "",
            exitStatusValue = 0,
        )
        val client = FakeCommandClient(channel = channel)
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = client,
            request = commandRequest(command = "uptime", timeoutMillis = 5_000),
        )

        assertEquals(
            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "ok",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
            result,
        )
        assertEquals("uptime", client.lastCommand)
        assertEquals(5_000L, channel.lastJoinTimeoutMillis)
        assertTrue(channel.closed)
    }

    @Test
    fun `returns timed out result and closes channel when exit status is unavailable`() {
        val channel = FakeCommandChannel(
            stdoutText = "partial",
            stderrText = "",
            exitStatusValue = null,
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(),
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut),
            result,
        )
        assertTrue(channel.closed)
    }

    @Test
    fun `returns channel open failure when channel cannot be opened`() {
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(openError = IllegalStateException("open failed")),
            request = commandRequest(),
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.ChannelOpenFailed),
            result,
        )
    }

    @Test
    fun `returns command execution failure and closes channel when execution fails after opening`() {
        val channel = FakeCommandChannel(
            joinError = IllegalStateException("join failed"),
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(),
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandExecutionFailed),
            result,
        )
        assertTrue(channel.closed)
    }

    private fun commandRequest(
        command: String = "uptime",
        timeoutMillis: Long = 30_000,
    ): SshCommandRequest {
        return SshCommandRequest(
            sessionHandle = sshSessionHandle(),
            command = command,
            timeoutMillis = timeoutMillis,
        )
    }

    private class FakeCommandClient(
        private val channel: SshjCommandChannel? = null,
        private val openError: RuntimeException? = null,
    ) : SshjCommandChannelClient {

        var lastCommand: String? = null
            private set

        override fun openCommandChannel(command: String): SshjCommandChannel {
            lastCommand = command
            openError?.let { throw it }
            return requireNotNull(channel)
        }
    }

    private class FakeCommandChannel(
        stdoutText: String = "",
        stderrText: String = "",
        private val exitStatusValue: Int? = 0,
        private val joinError: RuntimeException? = null,
    ) : SshjCommandChannel {

        override val stdout: InputStream = ByteArrayInputStream(stdoutText.toByteArray())

        override val stderr: InputStream = ByteArrayInputStream(stderrText.toByteArray())

        override val exitStatus: Int?
            get() = exitStatusValue

        var lastJoinTimeoutMillis: Long? = null
            private set

        var closed: Boolean = false
            private set

        override fun join(timeoutMillis: Long) {
            lastJoinTimeoutMillis = timeoutMillis
            joinError?.let { throw it }
        }

        override fun close() {
            closed = true
        }
    }
}
