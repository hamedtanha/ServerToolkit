package de.hamedtanha.servertoolkit.feature.ssh.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.feature.ssh.data.repository.RoomSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SshModule {

    @Binds
    @Singleton
    abstract fun bindSshConnectionService(
        service: SshjConnectionService,
    ): SshConnectionService

    @Binds
    @Singleton
    abstract fun bindSshHostTrustRepository(
        repository: RoomSshHostTrustRepository,
    ): SshHostTrustRepository
}
