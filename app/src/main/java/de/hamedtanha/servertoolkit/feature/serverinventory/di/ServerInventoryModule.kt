package de.hamedtanha.servertoolkit.feature.serverinventory.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.serverinventory.data.connection.ServerInventoryConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.serverinventory.data.repository.RoomServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServerInventoryModule {

    @Binds
    @Singleton
    abstract fun bindServerRepository(
        repository: RoomServerRepository,
    ): ServerRepository

    @Binds
    @Singleton
    abstract fun bindConnectionTargetResolver(
        resolver: ServerInventoryConnectionTargetResolver,
    ): ConnectionTargetResolver
}
