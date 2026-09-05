package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlinx.coroutines.CancellationException
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session

/**
 * Executes a non-interactive SSH command through a short-lived command channel.
 *
 * SSHJ clients, sessions, channels, streams, sockets, and exceptions must remain inside this
 * data-layer boundary.
 */
internal interface SshjCommandChannelExecutor {

    fun execute(
        commandClient: SshjCommandChannelClient,
        request: SshCommandRequest,
    ): SshCommandExecutionResult
}

/**
 * Minimal data-layer client boundary for opening command channels.
 *
 * This keeps the executor testable without exposing SSHJ types to domain or presentation code.
 */
internal interface SshjCommandChannelClient {

    fun openCommandChannel(command: String): SshjCommandChannel
}

/**
 * Minimal data-layer command channel boundary.
 *
 * Implementations own concrete SSHJ command/session resources and must close them during cleanup.
 */
internal interface SshjCommandChannel {

    val stdout: InputStream

    val stderr: InputStream

    val exitStatus: Int?

    fun join(timeoutMillis: Long)

    fun close()
}

internal class SshjNetworkCommandChannelExecutor(
    private val maxRetainedBytesPerStream: Int = DEFAULT_MAX_RETAINED_BYTES_PER_STREAM,
    private val nanoTimeProvider: () -> Long = System::nanoTime,
) : SshjCommandChannelExecutor {

    init {
        require(maxRetainedBytesPerStream > 0) {
            "SSH command retained-output limit must be positive."
        }
    }

    override fun execute(
        commandClient: SshjCommandChannelClient,
        request: SshCommandRequest,
    ): SshCommandExecutionResult {
        val deadline = CommandOperationDeadline(
            timeoutMillis = request.timeoutMillis,
            nanoTimeProvider = nanoTimeProvider,
        )
        var channel: SshjCommandChannel? = null
        var channelOpened = false
        var streamExecutor: ExecutorService? = null
        var stdoutFuture: Future<RetainedCommandStream>? = null
        var stderrFuture: Future<RetainedCommandStream>? = null

        return try {
            channel = commandClient.openCommandChannel(request.command)
            channelOpened = true
            deadline.requireRemainingMillis()

            streamExecutor = newCommandStreamExecutor()
            stdoutFuture = streamExecutor.submit<RetainedCommandStream> {
                channel.stdout.drainUtf8(maxRetainedBytesPerStream)
            }
            stderrFuture = streamExecutor.submit<RetainedCommandStream> {
                channel.stderr.drainUtf8(maxRetainedBytesPerStream)
            }

            channel.join(deadline.requireRemainingMillis())

            val exitStatus = channel.exitStatus
                ?: return SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut)

            val stdout = stdoutFuture.awaitWithin(deadline)
            val stderr = stderrFuture.awaitWithin(deadline)

            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = stdout.text,
                    stderr = stderr.text,
                    exitStatus = exitStatus,
                    stdoutTruncated = stdout.truncated,
                    stderrTruncated = stderr.truncated,
                ),
            )
        } catch (error: TimeoutException) {
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut)
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw CancellationException("SSH command execution was cancelled.").apply {
                initCause(error)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (channelOpened) {
                SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandExecutionFailed)
            } else {
                SshCommandExecutionResult.Failed(SshCommandExecutionError.ChannelOpenFailed)
            }
        } finally {
            runCatching {
                channel?.close()
            }
            stdoutFuture?.cancel(true)
            stderrFuture?.cancel(true)
            streamExecutor?.shutdownNow()
        }
    }

    private fun newCommandStreamExecutor(): ExecutorService {
        val threadCounter = AtomicInteger(0)
        return Executors.newFixedThreadPool(STREAM_DRAINER_COUNT) { runnable ->
            Thread(
                runnable,
                "ServerToolkit-ssh-command-stream-${threadCounter.incrementAndGet()}",
            ).apply {
                isDaemon = true
            }
        }
    }

    private companion object {
        const val DEFAULT_MAX_RETAINED_BYTES_PER_STREAM: Int = 256 * 1024
        const val STREAM_BUFFER_BYTES: Int = 8 * 1024
        const val STREAM_DRAINER_COUNT: Int = 2
    }

    private data class RetainedCommandStream(
        val text: String,
        val truncated: Boolean,
    )

    private class CommandOperationDeadline(
        timeoutMillis: Long,
        private val nanoTimeProvider: () -> Long,
    ) {
        private val deadlineNanos = nanoTimeProvider() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)

        fun requireRemainingMillis(): Long {
            val remainingNanos = deadlineNanos - nanoTimeProvider()
            if (remainingNanos <= 0L) {
                throw TimeoutException("SSH command operation deadline elapsed.")
            }

            return maxOf(
                1L,
                TimeUnit.NANOSECONDS.toMillis(remainingNanos),
            )
        }
    }

    private fun Future<RetainedCommandStream>.awaitWithin(
        deadline: CommandOperationDeadline,
    ): RetainedCommandStream {
        return try {
            get(
                deadline.requireRemainingMillis(),
                TimeUnit.MILLISECONDS,
            )
        } catch (error: ExecutionException) {
            val cause = error.cause ?: error
            when (cause) {
                is CancellationException -> throw cause
                is Exception -> throw cause
                else -> throw error
            }
        }
    }

    private fun InputStream.drainUtf8(
        maxRetainedBytes: Int,
    ): RetainedCommandStream {
        val retained = ByteArrayOutputStream(min(STREAM_BUFFER_BYTES, maxRetainedBytes))
        val buffer = ByteArray(STREAM_BUFFER_BYTES)
        var retainedBytes = 0
        var truncated = false

        while (true) {
            val readCount = read(buffer)
            if (readCount < 0) {
                break
            }
            if (readCount == 0) {
                continue
            }

            val remainingCapacity = maxRetainedBytes - retainedBytes
            if (remainingCapacity > 0) {
                val retainedCount = min(readCount, remainingCapacity)
                retained.write(buffer, 0, retainedCount)
                retainedBytes += retainedCount
                if (retainedCount < readCount) {
                    truncated = true
                }
            } else {
                truncated = true
            }
        }

        return RetainedCommandStream(
            text = retained.toByteArray().toString(Charsets.UTF_8),
            truncated = truncated,
        )
    }
}

internal class SshjCommandChannelClientAdapter(
    private val client: SSHClient,
) : SshjCommandChannelClient {

    override fun openCommandChannel(command: String): SshjCommandChannel {
        val session = client.startSession()

        return try {
            SshjCommandChannelAdapter(
                session = session,
                command = session.exec(command),
            )
        } catch (error: Exception) {
            runCatching {
                session.close()
            }
            throw error
        }
    }
}

private class SshjCommandChannelAdapter(
    private val session: Session,
    private val command: Session.Command,
) : SshjCommandChannel {

    override val stdout: InputStream
        get() = command.inputStream

    override val stderr: InputStream
        get() = command.errorStream

    override val exitStatus: Int?
        get() = command.exitStatus

    override fun join(timeoutMillis: Long) {
        command.join(timeoutMillis, TimeUnit.MILLISECONDS)
    }

    override fun close() {
        runCatching {
            command.close()
        }
        session.close()
    }
}
