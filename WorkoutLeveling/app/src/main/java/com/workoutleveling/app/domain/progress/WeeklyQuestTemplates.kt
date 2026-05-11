package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.QuestEntity
import java.time.LocalDate
import java.time.temporal.WeekFields

object WeeklyQuestTemplates {
    /** Kunci ISO week-based year + week, mis. `2026-W19`. */
    fun isoWeekPeriodKey(date: LocalDate): String {
        val iso = WeekFields.ISO
        val year = date.get(iso.weekBasedYear())
        val week = date.get(iso.weekOfWeekBasedYear())
        return "%d-W%02d".format(year, week)
    }

    fun weeklySessionsQuestId(weekKey: String): String = "weekly_sessions_$weekKey"

    fun weeklySessionsQuest(weekKey: String): QuestEntity =
        QuestEntity(
            id = weeklySessionsQuestId(weekKey),
            title = "3× sesi minggu ini",
            questType = "weekly",
            targetValue = 3,
            progressValue = 0,
            status = "active",
            periodKey = weekKey,
        )
}
