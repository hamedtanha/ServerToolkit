package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertTrue(requireNotNull(channel.lastJoinTimeoutMillis) <= 5_000L)
        assertTrue(requireNotNull(channel.lastJoinTimeoutMillis) > 0L)
        assertTrue(channel.closed)
    }

    @Test
    fun `drains stdout while waiting for command completion`() {
        val stdout = "x".repeat(128 * 1024)
        val channel = DrainDependentCommandChannel(
            stdoutText = stdout,
            stderrText = "",
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(timeoutMillis = 2_000),
        )

        assertTrue(result is SshCommandExecutionResult.Completed)
        val output = (result as SshCommandExecutionResult.Completed).output
        assertEquals(stdout, output.stdout)
        assertFalse(output.stdoutTruncated)
        assertTrue(channel.joinCompleted)
        assertTrue(channel.closed)
    }

    @Test
    fun `drains stderr while waiting for command completion`() {
        val stderr = "e".repeat(128 * 1024)
        val channel = DrainDependentCommandChannel(
            stdoutText = "",
            stderrText = stderr,
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(timeoutMillis = 2_000),
        )

        assertTrue(result is SshCommandExecutionResult.Completed)
        val output = (result as SshCommandExecutionResult.Completed).output
        assertEquals(stderr, output.stderr)
        assertFalse(output.stderrTruncated)
        assertTrue(channel.joinCompleted)
        assertTrue(channel.closed)
    }

    @Test
    fun `drains mixed output beyond retention limit while retaining bounded bytes`() {
        val stdout = "0123456789abcdefghijklmnopqrstuvwxyz"
        val stderr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9876543210"
        val channel = DrainDependentCommandChannel(
            stdoutText = stdout,
            stderrText = stderr,
        )
        val executor = SshjNetworkCommandChannelExecutor(
            maxRetainedBytesPerStream = 12,
        )

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(timeoutMillis = 2_000),
        )

        assertEquals(
            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = stdout.take(12),
                    stderr = stderr.take(12),
                    exitStatus = 0,
                    stdoutTruncated = true,
                    stderrTruncated = true,
                ),
            ),
            result,
        )
        assertEquals(stdout.toByteArray().size, channel.stdoutBytesRead)
        assertEquals(stderr.toByteArray().size, channel.stderrBytesRead)
        assertTrue(channel.joinCompleted)
        assertTrue(channel.closed)
    }

    @Test
    fun `default retained output limit matches production baseline and still drains excess bytes`() {
        val stdout = "x".repeat(SSH_COMMAND_MAX_RETAINED_BYTES_PER_STREAM + 32)
        val channel = DrainDependentCommandChannel(
            stdoutText = stdout,
            stderrText = "",
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(timeoutMillis = 2_000),
        )

        assertTrue(result is SshCommandExecutionResult.Completed)
        val output = (result as SshCommandExecutionResult.Completed).output
        assertEquals(
            SSH_COMMAND_MAX_RETAINED_BYTES_PER_STREAM,
            output.stdout.toByteArray().size,
        )
        assertTrue(output.stdoutTruncated)
        assertEquals(stdout.toByteArray().size, channel.stdoutBytesRead)
        assertTrue(channel.joinCompleted)
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
    fun `bounds missing stdout EOF by command operation deadline`() {
        val blockingStdout = BlockingUntilClosedInputStream()
        val channel = FakeCommandChannel(
            stdoutStream = blockingStdout,
            stderrText = "",
            exitStatusValue = 0,
            onClose = blockingStdout::release,
        )
        val executor = SshjNetworkCommandChannelExecutor()

        val startedAt = System.nanoTime()
        val result = executor.execute(
            commandClient = FakeCommandClient(channel = channel),
            request = commandRequest(timeoutMillis = 75),
        )
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut),
            result,
        )
        assertTrue(blockingStdout.readStarted.await(1, TimeUnit.SECONDS))
        assertTrue(channel.closed)
        assertTrue(elapsedMillis < 2_000)
    }

    @Test
    fun `thread interruption during blocking stream read preserves cancellation and closes channel`() {
        val blockingStdout = BlockingUntilClosedInputStream()
        val channel = FakeCommandChannel(
            stdoutStream = blockingStdout,
            stderrText = "",
            exitStatusValue = 0,
            onClose = blockingStdout::release,
        )
        val executor = SshjNetworkCommandChannelExecutor()
        val observedFailure = AtomicReference<Throwable?>(null)
        val executionThread = Thread {
            try {
                executor.execute(
                    commandClient = FakeCommandClient(channel = channel),
                    request = commandRequest(timeoutMillis = 5_000),
                )
                fail("Expected CancellationException")
            } catch (error: Throwable) {
                observedFailure.set(error)
            }
        }

        executionThread.start()
        assertTrue(blockingStdout.readStarted.await(1, TimeUnit.SECONDS))
        executionThread.interrupt()
        executionThread.join(2_000)

        assertFalse(executionThread.isAlive)
        assertTrue(observedFailure.get() is CancellationException)
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

    @Test
    fun `preserves cancellation when cleanup fails after execution is cancelled`() {
        val channel = FakeCommandChannel(
            joinError = CancellationException("cancelled"),
            closeError = IllegalStateException("close failed"),
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
        stdoutStream: InputStream? = null,
        stderrStream: InputStream? = null,
        private val exitStatusValue: Int? = 0,
        private val joinError: Exception? = null,
        private val closeError: Exception? = null,
        private val onClose: () -> Unit = {},
    ) : SshjCommandChannel {

        override val stdout: InputStream = stdoutStream ?: inputStreamFor(
            text = stdoutText,
            readError = stdoutReadError,
        )

        override val stderr: InputStream = stderrStream ?: inputStreamFor(
            text = stderrText,
            readError = stderrReadError,
        )

        override val exitStatus: Int?
            get() = exitStatusValue

        var lastJoinTimeoutMillis: Long? = null
            private set

        @Volatile
        var closed: Boolean = false
            private set

        override fun join(timeoutMillis: Long) {
            lastJoinTimeoutMillis = timeoutMillis
            joinError?.let { throw it }
        }

        override fun close() {
            closed = true
            onClose()
            closeError?.let { throw it }
        }
    }

    private class DrainDependentCommandChannel(
        stdoutText: String,
        stderrText: String,
    ) : SshjCommandChannel {

        private val streamsDrained = CountDownLatch(2)
        private val stdoutInput = TrackingInputStream(
            bytes = stdoutText.toByteArray(),
            onEof = streamsDrained::countDown,
        )
        private val stderrInput = TrackingInputStream(
            bytes = stderrText.toByteArray(),
            onEof = streamsDrained::countDown,
        )

        override val stdout: InputStream = stdoutInput
        override val stderr: InputStream = stderrInput

        @Volatile
        var joinCompleted: Boolean = false
            private set

        @Volatile
        var closed: Boolean = false
            private set

        val stdoutBytesRead: Int
            get() = stdoutInput.bytesRead

        val stderrBytesRead: Int
            get() = stderrInput.bytesRead

        override val exitStatus: Int?
            get() = if (joinCompleted) 0 else null

        override fun join(timeoutMillis: Long) {
            joinCompleted = streamsDrained.await(timeoutMillis, TimeUnit.MILLISECONDS)
        }

        override fun close() {
            closed = true
        }
    }

    private class TrackingInputStream(
        private val bytes: ByteArray,
        private val onEof: () -> Unit,
    ) : InputStream() {

        private var position = 0
        private var eofReported = false

        @Volatile
        var bytesRead: Int = 0
            private set

        override fun read(): Int {
            val buffer = ByteArray(1)
            val count = read(buffer, 0, 1)
            return if (count < 0) -1 else buffer[0].toInt() and 0xFF
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (position >= bytes.size) {
                reportEofOnce()
                return -1
            }

            val count = minOf(length, bytes.size - position)
            bytes.copyInto(
                destination = buffer,
                destinationOffset = offset,
                startIndex = position,
                endIndex = position + count,
            )
            position += count
            bytesRead += count
            return count
        }

        private fun reportEofOnce() {
            if (!eofReported) {
                eofReported = true
                onEof()
            }
        }
    }

    private class BlockingUntilClosedInputStream : InputStream() {
        val readStarted = CountDownLatch(1)
        private val released = CountDownLatch(1)

        override fun read(): Int {
            readStarted.countDown()
            return try {
                released.await()
                -1
            } catch (error: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Blocking read interrupted", error)
            }
        }

        fun release() {
            released.countDown()
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
