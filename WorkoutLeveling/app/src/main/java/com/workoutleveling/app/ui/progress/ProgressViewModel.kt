package com.workoutleveling.app.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.PlayerStateEntity
import com.workoutleveling.app.data.local.db.QuestEntity
import com.workoutleveling.app.domain.progress.DailyQuestTemplates
import com.workoutleveling.app.domain.progress.ProgressRules
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerProgressUi(
    val xp: Int,
    val level: Int,
    val rank: String,
    val streak: Int,
    val xpIntoLevel: Int,
    val xpToNext: Int,
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WorkoutLevelingApp
    private val playerDao = app.database.playerStateDao()
    private val questDao = app.database.questDao()

    private val todayKey = LocalDate.now().toString()

    init {
        viewModelScope.launch {
            ensureTodayDailyQuest()
        }
    }

    val player: StateFlow<PlayerProgressUi> =
        playerDao.observe()
            .map { row -> toUi(row) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                toUi(null),
            )

    val dailyQuests: StateFlow<List<QuestEntity>> =
        questDao.observeForDay(todayKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summaryText: StateFlow<String> =
        combine(player, dailyQuests) { _, quests ->
            val completed = quests.count { it.status == "completed" }
            val total = quests.size
            when {
                total == 0 -> "Belum ada quest harian untuk hari ini."
                else -> "Quest harian: $completed / $total selesai."
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private suspend fun ensureTodayDailyQuest() {
        val day = LocalDate.now()
        val id = DailyQuestTemplates.todaySessionQuestId(day)
        if (questDao.getById(id) == null) {
            questDao.upsert(DailyQuestTemplates.todaySessionQuest(day))
        }
    }

    private fun toUi(row: PlayerStateEntity?): PlayerProgressUi {
        val xp = row?.xpTotal ?: 0
        val level = ProgressRules.levelFromXp(xp)
        return PlayerProgressUi(
            xp = xp,
            level = level,
            rank = row?.rankCode ?: "E",
            streak = row?.streakDays ?: 0,
            xpIntoLevel = ProgressRules.xpIntoCurrentLevel(xp),
            xpToNext = ProgressRules.xpRemainingToNextLevel(xp),
        )
    }
}
