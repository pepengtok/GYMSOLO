package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.QuestEntity
import java.time.LocalDate

object DailyQuestTemplates {
    fun todaySessionQuestId(date: LocalDate): String = "daily_session_$date"

    fun todaySessionQuest(date: LocalDate): QuestEntity {
        val key = date.toString()
        return QuestEntity(
            id = todaySessionQuestId(date),
            title = "Selesaikan 1 sesi hari ini",
            questType = "daily",
            targetValue = 1,
            progressValue = 0,
            status = "active",
            periodKey = key,
        )
    }
}
