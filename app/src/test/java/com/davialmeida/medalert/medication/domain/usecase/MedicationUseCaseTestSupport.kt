package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

internal val today = LocalDate.of(2026, 10, 16)
internal fun sampleMedication(
    id: Long = 1,
    seriesId: Long = id,
    start: LocalDate = today.minusDays(5),
    end: LocalDate? = null,
    name: String = "Paracetamol",
    dosage: BigDecimal = BigDecimal("500"),
    frequency: Frequency = Frequency.Daily(listOf(LocalTime.of(8, 0)))
) = Medication(
    id = id, seriesId = seriesId, profileId = 1, name = name,
    dosage = Dosage(dosage, DosageUnit.MG), form = MedicationForm.TABLET,
    frequency = frequency, startDate = start, endDate = end,
    color = MedicationColor.BLUE, notes = "", remindersEnabled = true
)

internal class FakeMedicationRepository : MedicationRepository {
    val data = mutableListOf<Medication>()
    var nextId = 10L
    var lastDeleted: Pair<Long, Long>? = null
    override fun observeByProfile(profileId: Long): Flow<List<Medication>> =
        flowOf(data.filter { it.profileId == profileId })

    override fun observeActiveOn(profileId: Long, date: LocalDate): Flow<List<Medication>> =
        flowOf(data.filter { it.profileId == profileId && it.isActiveOn(date) })

    override fun observeById(id: Long): Flow<Medication?> = flowOf(data.find { it.id == id })
    override suspend fun findById(id: Long): Medication? = data.find { it.id == id }
    override suspend fun findVersionsBySeries(profileId: Long, seriesId: Long): List<Medication> =
        data.filter { it.profileId == profileId && it.seriesId == seriesId }

    override suspend fun create(medication: Medication): Long {
        val id = nextId++
        data.add(medication.copy(id = id, seriesId = id))
        return id
    }

    override suspend fun createVersion(medication: Medication): Long {
        val id = nextId++
        data.add(medication.copy(id = id, seriesId = medication.seriesId))
        return id
    }

    override suspend fun update(medication: Medication) {
        val index = data.indexOfFirst { it.id == medication.id }
        if (index >= 0) data[index] = medication
    }

    override suspend fun updateAndCreateVersion(
        previousVersion: Medication, newVersion: Medication, previousEndDate: LocalDate
    ): Long {
        update(previousVersion.copy(endDate = previousEndDate))
        return createVersion(newVersion.copy(id = 0, seriesId = previousVersion.seriesId))
    }

    override suspend fun deleteSeries(profileId: Long, seriesId: Long) {
        lastDeleted = profileId to seriesId
        data.removeAll { it.profileId == profileId && it.seriesId == seriesId }
    }

    override suspend fun findAllWithReminders(): List<Medication> =
        data.filter { it.remindersEnabled }

    override fun observeAllWithReminders(): Flow<List<Medication>> =
        flowOf(data.filter { it.remindersEnabled })
}

internal class FakeProfileRepository(
    private val active: Profile?,
    private val profiles: List<Profile> = emptyList()
) : ProfileRepository {
    override fun observeAll(): Flow<List<Profile>> = flowOf(emptyList())
    override fun observeById(id: Long): Flow<Profile?> =
        flowOf(profiles.firstOrNull { it.id == id })

    override suspend fun create(profile: Profile): Long = profile.id
    override suspend fun createIfEmpty(profile: Profile): Long? = profile.id
    override suspend fun update(profile: Profile) {}
    override suspend fun delete(profile: Profile) {}
    override fun observeActiveProfile(): Flow<Profile?> = flowOf(active)
    override suspend fun setActiveProfileId(profileId: Long?) {}
}

