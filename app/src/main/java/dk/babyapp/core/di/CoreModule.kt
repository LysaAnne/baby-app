package dk.babyapp.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.babyapp.core.logging.AndroidAppLogger
import dk.babyapp.core.logging.AppLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Binds
    @Singleton
    abstract fun bindAppLogger(implementation: AndroidAppLogger): AppLogger
}

