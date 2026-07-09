package de.hamedtanha.servertoolkit.feature.ssh.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.feature.ssh.data.repository.RoomSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.data.repository.RoomSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjCommandExecutionPlanningService
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.data.service.SshjSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionPlanningService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
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
    abstract fun bindSshHostKeyObservationService(
        service: SshjHostKeyObservationService,
    ): SshHostKeyObservationService

    @Binds
    @Singleton
    abstract fun bindSshSessionLifecycleService(
        service: SshjSessionLifecycleService,
    ): SshSessionLifecycleService

    @Binds
    @Singleton
    abstract fun bindSshCommandExecutionPlanningService(
        service: SshjCommandExecutionPlanningService,
    ): SshCommandExecutionPlanningService

    @Binds
    @Singleton
    abstract fun bindSshCommandExecutionService(
        service: SshjCommandExecutionService,
    ): SshCommandExecutionService

    @Binds
    @Singleton
    abstract fun bindSshHostTrustRepository(
        repository: RoomSshHostTrustRepository,
    ): SshHostTrustRepository

    @Binds
    @Singleton
    abstract fun bindSshConnectionHistoryRepository(
        repository: RoomSshConnectionHistoryRepository,
    ): SshConnectionHistoryRepository
}
