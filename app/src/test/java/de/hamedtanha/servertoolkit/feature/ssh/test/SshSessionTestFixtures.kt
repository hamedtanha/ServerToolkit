package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle

fun sshSessionHandle(
    sessionId: String = "session-1",
    serverId: String = "server-1",
    host: String = "example.com",
    port: Int = 22,
    username: String = "admin",
): SshSessionHandle {
    return SshSessionHandle(
        sessionId = sessionId,
        serverId = serverId,
        host = host,
        port = port,
        username = username,
    )
}

fun sshConnectedResult(
    sessionId: String = "session-1",
): SshConnectionResult.Connected {
    return SshConnectionResult.Connected(
        sessionHandle = sshSessionHandle(sessionId = sessionId),
    )
}
