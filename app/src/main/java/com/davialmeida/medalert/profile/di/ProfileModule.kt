package com.davialmeida.medalert.profile.di

import com.davialmeida.medalert.profile.data.repository.ProfileRepositoryImpl
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        repository: ProfileRepositoryImpl
    ): ProfileRepository
}