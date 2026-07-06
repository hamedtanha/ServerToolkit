package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import java.io.InputStream
import java.util.concurrent.TimeUnit
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

internal class SshjNetworkCommandChannelExecutor : SshjCommandChannelExecutor {

    override fun execute(
        commandClient: SshjCommandChannelClient,
        request: SshCommandRequest,
    ): SshCommandExecutionResult {
        var channel: SshjCommandChannel? = null
        var channelOpened = false

        return try {
            channel = commandClient.openCommandChannel(request.command)
            channelOpened = true

            channel.join(request.timeoutMillis)

            val exitStatus = channel.exitStatus
                ?: return SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut)

            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = channel.stdout.readUtf8(),
                    stderr = channel.stderr.readUtf8(),
                    exitStatus = exitStatus,
                ),
            )
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
        }
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

private fun InputStream.readUtf8(): String {
    return readBytes().toString(Charsets.UTF_8)
}
