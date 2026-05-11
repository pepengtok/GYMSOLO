package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.QuestDao
import java.time.LocalDate

object QuestBootstrap {
    suspend fun ensureTodayAndWeek(questDao: QuestDao) {
        val day = LocalDate.now()
        val dailyId = DailyQuestTemplates.todaySessionQuestId(day)
        if (questDao.getById(dailyId) == null) {
            questDao.upsert(DailyQuestTemplates.todaySessionQuest(day))
        }
        val weekKey = WeeklyQuestTemplates.isoWeekPeriodKey(day)
        val weeklyId = WeeklyQuestTemplates.weeklySessionsQuestId(weekKey)
        if (questDao.getById(weeklyId) == null) {
            questDao.upsert(WeeklyQuestTemplates.weeklySessionsQuest(weekKey))
        }
    }
}
