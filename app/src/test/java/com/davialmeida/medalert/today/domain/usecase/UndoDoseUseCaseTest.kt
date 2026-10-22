package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class UndoDoseUseCaseTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val repository = FakeDoseLogRepository()
    private val useCase = UndoDoseUseCase(repository)

    private fun log(
        medicationId: Long,
        dateTime: LocalDateTime
    ) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = dateTime,
        status = DoseLogStatus.TAKEN,
        loggedAt = Instant.parse("2026-10-16T10:00:00Z"),
        takenAt = Instant.parse("2026-10-16T10:00:00Z")
    )

    @Test
    fun `undo removes the log of that dose`() = runTest {
        repository.addLog(log(1L, date.atTime(8, 0)))

        val result = useCase(1L, date.atTime(8, 0))

        assertEquals(AppResult.Success(Unit), result)
        assertTrue(repository.logs.isEmpty())
    }

    @Test
    fun `undo keeps logs of other doses and other medications`() = runTest {
        repository.addLog(log(1L, date.atTime(8, 0)))
        repository.addLog(log(1L, date.atTime(20, 0)))
        repository.addLog(log(2L, date.atTime(8, 0)))

        useCase(1L, date.atTime(8, 0))

        assertEquals(2, repository.logs.size)
        assertTrue(
            repository.logs.none {
                it.medicationId == 1L &&
                        it.scheduledDateTime == date.atTime(8, 0)
            }
        )
    }

    @Test
    fun `undo of a dose without log succeeds`() = runTest {
        val result = useCase(1L, date.atTime(8, 0))

        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `repository failure returns UnexpectedAppError`() = runTest {
        repository.failOnDelete = true

        val result = useCase(1L, date.atTime(8, 0))

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}