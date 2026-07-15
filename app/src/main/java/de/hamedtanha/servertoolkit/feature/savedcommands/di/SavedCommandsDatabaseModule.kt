package de.hamedtanha.servertoolkit.feature.savedcommands.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.dao.SavedCommandDao

@Module
@InstallIn(SingletonComponent::class)
object SavedCommandsDatabaseModule {

    @Provides
    fun provideSavedCommandDao(
        database: ServerToolkitDatabase,
    ): SavedCommandDao {
        return database.savedCommandDao()
    }
}
