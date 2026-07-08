package de.hamedtanha.servertoolkit.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.core.database.MIGRATION_1_2
import de.hamedtanha.servertoolkit.core.database.MIGRATION_2_3
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideServerToolkitDatabase(
        @ApplicationContext context: Context,
    ): ServerToolkitDatabase {
        return Room.databaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
            DATABASE_NAME,
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    private const val DATABASE_NAME = "server_toolkit.db"
}
