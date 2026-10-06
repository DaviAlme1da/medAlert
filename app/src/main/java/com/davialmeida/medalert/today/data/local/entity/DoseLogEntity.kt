package com.davialmeida.medalert.today.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import java.time.Instant

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["medicationId", "scheduledDate", "scheduledTime"],
            unique = true
        )
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val medicationId: Long,

    val scheduledDate: Long,

    val scheduledTime: Int,

    val status: String,

    val loggedAt: Instant,

    val takenAt: Instant?,

    val notes: String?
)