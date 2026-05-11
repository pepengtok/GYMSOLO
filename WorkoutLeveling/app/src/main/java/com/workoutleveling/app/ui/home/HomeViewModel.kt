package com.workoutleveling.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.domain.progress.PlayerRankSync
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.domain.progress.QuestBootstrap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionHistoryItem(
    val title: String,
    val subtitle: String,
)

data class PlayerSummaryUi(
    val level: Int,
    val xp: Int,
    val streak: Int,
    val rank: String,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WorkoutLevelingApp
    private val dao = app.database.workoutSessionDao()
    private val playerDao = app.database.playerStateDao()
    private val questDao = app.database.questDao()
    private val dateFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            QuestBootstrap.ensureTodayAndWeek(questDao)
            PlayerRankSync.refreshRankFromHistory(dao, playerDao)
        }
    }

    val sessionCount: StateFlow<Int> = dao.observeAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recentSessions: StateFlow<List<SessionHistoryItem>> = dao.observeAll()
        .map { sessions ->
            sessions.take(5).map { session ->
                val whenText = dateFmt.format(Date(session.startedAtEpochMs))
                val rpePart = session.effortRpe?.let { " · RPE $it" }.orEmpty()
                SessionHistoryItem(
                    title = "Gate ${session.type}",
                    subtitle = "Mulai $whenText$rpePart",
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playerSummary: StateFlow<PlayerSummaryUi> = playerDao.observe()
        .map { row ->
            val xp = row?.xpTotal ?: 0
            PlayerSummaryUi(
                level = ProgressRules.levelFromXp(xp),
                xp = xp,
                streak = row?.streakDays ?: 0,
                rank = row?.rankCode ?: "E",
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PlayerSummaryUi(1, 0, 0, "E"),
        )

    val baselineDone: StateFlow<Boolean> = app.userPreferences.baselineDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val homeTipsDismissed: StateFlow<Boolean> = app.userPreferences.isHomeTipsDismissed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun dismissHomeTips() {
        viewModelScope.launch { app.userPreferences.setHomeTipsDismissed(true) }
    }
}
