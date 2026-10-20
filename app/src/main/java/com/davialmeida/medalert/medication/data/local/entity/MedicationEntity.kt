
package com.davialmeida.medalert.medication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.davialmeida.medalert.profile.data.local.entity.ProfileEntity
import java.math.BigDecimal

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["seriesId"])
    ]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seriesId: Long = 0,
    val profileId: Long,
    val name: String,
    val doseAmount: BigDecimal,
    val doseUnit: String,
    val form: String,
    val frequencyType: String,
    val frequencyTimes: String,
    val frequencyDays: String?,
    val frequencyIntervalHours: Int?,
    val frequencyFirstTime: String?,
    val startDate: Long,
    val endDate: Long?,
    val color: String,
    val notes: String,
    val remindersEnabled: Boolean
)
