package com.davialmeida.medalert.core.di

import android.content.Context
import androidx.room.Room
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.core.database.MIGRATION_1_2
import com.davialmeida.medalert.medication.data.local.dao.MedicationDao
import com.davialmeida.medalert.profile.data.local.dao.ProfileDao
import com.davialmeida.medalert.today.data.local.dao.DoseLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "medalert.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(
        database: AppDatabase
    ): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(
        database: AppDatabase
    ): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    @Singleton
    fun provideDoseLogDao(
        database: AppDatabase
    ): DoseLogDao {
        return database.doseLogDao()
    }
}