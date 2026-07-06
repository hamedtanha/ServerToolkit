package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
    fun `keeps completed result when cleanup fails after successful completion`() {
        val channel = FakeCommandChannel(
            stdoutText = "ok",
            stderrText = "",
            exitStatusValue = 0,
            closeError = IllegalStateException("close failed"),
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(),
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
        assertTrue(channel.closed)
    }

    @Test
    fun `keeps timed out result when cleanup fails after timeout`() {
        val channel = FakeCommandChannel(
            stdoutText = "partial",
            stderrText = "",
            exitStatusValue = null,
            closeError = IllegalStateException("close failed"),
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

    @Test
    fun `returns command execution failure and closes channel when stdout cannot be read`() {
        val channel = FakeCommandChannel(
            stdoutReadError = IOException("stdout failed"),
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

    @Test
    fun `returns command execution failure and closes channel when stderr cannot be read`() {
        val channel = FakeCommandChannel(
            stdoutText = "ok",
            stderrReadError = IOException("stderr failed"),
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

    @Test
    fun `preserves cancellation and closes channel when execution is cancelled after opening`() {
        val channel = FakeCommandChannel(
            joinError = CancellationException("cancelled"),
        )
        val executor = SshjNetworkCommandChannelExecutor()

        try {
            executor.execute(
                commandClient = FakeCommandClient(channel = channel),
                request = commandRequest(),
            )
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }

        assertTrue(channel.closed)
    }

    @Test(expected = CancellationException::class)
    fun `preserves cancellation when command channel opening is cancelled`() {
        val executor = SshjNetworkCommandChannelExecutor()

        executor.execute(
            commandClient = FakeCommandClient(openError = CancellationException("cancelled")),
            request = commandRequest(),
        )
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
        private val openError: Exception? = null,
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
        stdoutReadError: IOException? = null,
        stderrReadError: IOException? = null,
        private val exitStatusValue: Int? = 0,
        private val joinError: Exception? = null,
        private val closeError: Exception? = null,
    ) : SshjCommandChannel {

        override val stdout: InputStream = inputStreamFor(
            text = stdoutText,
            readError = stdoutReadError,
        )

        override val stderr: InputStream = inputStreamFor(
            text = stderrText,
            readError = stderrReadError,
        )

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
            closeError?.let { throw it }
        }
    }

    private companion object {

        fun inputStreamFor(
            text: String,
            readError: IOException?,
        ): InputStream {
            return if (readError == null) {
                ByteArrayInputStream(text.toByteArray())
            } else {
                FailingInputStream(readError)
            }
        }
    }

    private class FailingInputStream(
        private val error: IOException,
    ) : InputStream() {

        override fun read(): Int {
            throw error
        }
    }
}
