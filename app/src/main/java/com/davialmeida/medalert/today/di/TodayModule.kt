package com.davialmeida.medalert.today.di

import com.davialmeida.medalert.today.data.repository.DoseLogRepositoryImpl
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TodayModule {

    @Binds
    abstract fun bindDoseLogRepository(
        implementation: DoseLogRepositoryImpl
    ): DoseLogRepository
}