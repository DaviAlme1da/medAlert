package com.davialmeida.medalert.medication.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

sealed interface Frequency {

    fun calculateTimesForDate(date: LocalDate): List<LocalTime>

    companion object {
        val VALID_EVERY_X_HOURS_INTERVALS = setOf(
            1, 2, 3, 4, 6, 8, 12, 24
        )
    }

    data class Daily(
        val times: List<LocalTime>
    ) : Frequency {

        override fun calculateTimesForDate(
            date: LocalDate
        ): List<LocalTime> {
            return times.sorted()
        }
    }

    data class SpecificDays(
        val daysOfWeek: Set<DayOfWeek>,
        val times: List<LocalTime>
    ) : Frequency {

        override fun calculateTimesForDate(
            date: LocalDate
        ): List<LocalTime> {

            if (date.dayOfWeek !in daysOfWeek) {
                return emptyList()
            }

            return times.sorted()
        }
    }

    data class EveryXHours(
        val intervalHours: Int,
        val firstTime: LocalTime
    ) : Frequency {

        override fun calculateTimesForDate(
            date: LocalDate
        ): List<LocalTime> {

            if (intervalHours !in VALID_EVERY_X_HOURS_INTERVALS) {
                return emptyList()
            }

            val totalDoses = 24 / intervalHours
            val times = mutableListOf<LocalTime>()

            var currentTime = firstTime

            repeat(totalDoses) {
                times.add(currentTime)

                currentTime = currentTime.plusHours(
                    intervalHours.toLong()
                )
            }

            return times.sorted()
        }
    }
}