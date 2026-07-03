package de.hamedtanha.servertoolkit.feature.serverinventory.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.feature.serverinventory.data.repository.InMemoryServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ServerInventoryModule {

    @Binds
    abstract fun bindServerRepository(
        repository: InMemoryServerRepository,
    ): ServerRepository
}
