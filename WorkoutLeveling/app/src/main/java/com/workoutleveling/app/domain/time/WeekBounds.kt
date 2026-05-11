package com.workoutleveling.app.domain.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object WeekBounds {
    /** Senin 00:00 inklusif → senin minggu depan eksklusif, epoch ms. Minggu mengikuti kalender ISO. */
    fun currentIsoWeekRangeMillis(zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
        val today = LocalDate.now(zone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val nextMonday = monday.plusWeeks(1)
        val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = nextMonday.atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
