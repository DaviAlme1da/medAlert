package com.davialmeida.medalert.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.davialmeida.medalert.medication.data.local.dao.MedicationDao
import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import com.davialmeida.medalert.profile.data.local.dao.ProfileDao
import com.davialmeida.medalert.profile.data.local.entity.ProfileEntity
import com.davialmeida.medalert.today.data.local.dao.DoseLogDao
import com.davialmeida.medalert.today.data.local.entity.DoseLogEntity

@Database(
    entities = [
        ProfileEntity::class,
        MedicationEntity::class,
        DoseLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    abstract fun medicationDao(): MedicationDao

    abstract fun doseLogDao(): DoseLogDao
}