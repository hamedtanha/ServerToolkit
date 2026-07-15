package de.hamedtanha.servertoolkit.feature.savedcommands.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.feature.savedcommands.data.repository.RoomSavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SavedCommandsModule {

    @Binds
    @Singleton
    abstract fun bindSavedCommandRepository(
        repository: RoomSavedCommandRepository,
    ): SavedCommandRepository
}
