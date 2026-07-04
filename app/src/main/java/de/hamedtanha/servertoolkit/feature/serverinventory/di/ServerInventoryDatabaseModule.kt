package de.hamedtanha.servertoolkit.feature.serverinventory.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao.ServerDao

@Module
@InstallIn(SingletonComponent::class)
object ServerInventoryDatabaseModule {

    @Provides
    fun provideServerDao(
        database: ServerToolkitDatabase,
    ): ServerDao {
        return database.serverDao()
    }
}
