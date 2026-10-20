package com.davialmeida.medalert.medication.di

import com.davialmeida.medalert.medication.data.repository.MedicationRepositoryImpl
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicationModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        repository: MedicationRepositoryImpl
    ): MedicationRepository
}