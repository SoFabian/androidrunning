package com.sofa.nerdrunning.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RunDatabaseModule {

    @Provides
    fun provideRunDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RunDatabase::class.java,
        "runs_database"
    ).build()

    @Provides
    fun provideRunDao(db: RunDatabase) = db.runDao()

}
