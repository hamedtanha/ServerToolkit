package de.hamedtanha.servertoolkit.feature.ssh.data.repository

import de.hamedtanha.servertoolkit.feature.ssh.data.local.dao.SshTrustedHostKeyDao
import de.hamedtanha.servertoolkit.feature.ssh.data.mapper.toDomain
import de.hamedtanha.servertoolkit.feature.ssh.data.mapper.toEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSshHostTrustRepository @Inject constructor(
    private val trustedHostKeyDao: SshTrustedHostKeyDao,
) : SshHostTrustRepository {

    override suspend fun getTrustedHostKey(
        endpoint: SshHostEndpoint,
    ): SshTrustedHostKey? {
        return trustedHostKeyDao.getTrustedHostKey(
            serverId = endpoint.serverId,
            host = endpoint.host,
            port = endpoint.port,
        )?.toDomain()
    }

    override suspend fun saveTrustedHostKey(
        trustedHostKey: SshTrustedHostKey,
    ) {
        trustedHostKeyDao.insertTrustedHostKey(trustedHostKey.toEntity())
    }

    override suspend fun removeTrustedHostKey(
        endpoint: SshHostEndpoint,
    ) {
        trustedHostKeyDao.deleteTrustedHostKey(
            serverId = endpoint.serverId,
            host = endpoint.host,
            port = endpoint.port,
        )
    }
}
