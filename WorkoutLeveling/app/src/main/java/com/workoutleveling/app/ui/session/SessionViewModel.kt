package com.workoutleveling.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.ExerciseSetEntity
import com.workoutleveling.app.data.local.db.PlayerStateEntity
import com.workoutleveling.app.data.local.db.WorkoutSessionEntity
import com.workoutleveling.app.domain.catalog.CatalogExercise
import com.workoutleveling.app.domain.catalog.WorkoutCatalog
import com.workoutleveling.app.domain.progress.DailyQuestTemplates
import com.workoutleveling.app.domain.progress.ProgressRules
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DraftExerciseSet(
    val rowId: Long,
    val exerciseName: String = "",
    val reps: String = "",
    val weightKg: String = "",
)

data class SessionUiState(
    val type: String = "A",
    val notes: String = "",
    val sets: List<DraftExerciseSet> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as WorkoutLevelingApp).database
    private val sessionDao = db.workoutSessionDao()
    private val setDao = db.exerciseSetDao()
    private val playerStateDao = db.playerStateDao()
    private val questDao = db.questDao()

    private val sessionStartedAtMs: Long = System.currentTimeMillis()

    private var nextDraftRowId = 1L

    private fun newDraft(
        exerciseName: String = "",
        reps: String = "",
        weightKg: String = "",
    ) = DraftExerciseSet(
        rowId = nextDraftRowId++,
        exerciseName = exerciseName,
        reps = reps,
        weightKg = weightKg,
    )

    private val _uiState = MutableStateFlow(SessionUiState(sets = listOf(newDraft())))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun setType(type: String) {
        _uiState.update { it.copy(type = type) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun addSet() {
        _uiState.update { it.copy(sets = it.sets + newDraft()) }
    }

    fun removeSet(index: Int) {
        _uiState.update { state ->
            if (state.sets.size <= 1) return@update state
            state.copy(sets = state.sets.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateSet(index: Int, update: (DraftExerciseSet) -> DraftExerciseSet) {
        _uiState.update { state ->
            val next = state.sets.mapIndexed { i, set ->
                if (i == index) update(set) else set
            }
            state.copy(sets = next)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun applyTemplateDayA() {
        applyTemplate("A", WorkoutCatalog.dayA, "Template Day A diterapkan.")
    }

    fun applyTemplateDayB() {
        applyTemplate("B", WorkoutCatalog.dayB, "Template Day B diterapkan.")
    }

    fun applyTemplateCardio() {
        applyTemplate("Cardio", WorkoutCatalog.cardio, "Template cardio diterapkan.")
    }

    private fun applyTemplate(type: String, items: List<CatalogExercise>, msg: String) {
        viewModelScope.launch {
            val drafts = items.map { ex -> draftFromCatalog(ex) }
            _uiState.update {
                it.copy(
                    type = type,
                    sets = drafts,
                    message = msg,
                )
            }
        }
    }

    /** Dipanggil saat user memilih nama dari menu katalog. */
    fun applyPickedExercise(index: Int, name: String) {
        viewModelScope.launch {
            val last = setDao.getLatestSetForExerciseName(name)
            val sug = WorkoutCatalog.suggestedRepsForName(name)
            _uiState.update { state ->
                val next = state.sets.mapIndexed { i, set ->
                    if (i != index) return@mapIndexed set
                    set.copy(
                        exerciseName = name,
                        reps = last?.reps?.toString() ?: sug.orEmpty(),
                        weightKg = formatKg(last?.weightKg),
                    )
                }
                state.copy(sets = next, message = null)
            }
        }
    }

    private suspend fun draftFromCatalog(ex: CatalogExercise): DraftExerciseSet {
        val last = setDao.getLatestSetForExerciseName(ex.displayName)
        return newDraft(
            exerciseName = ex.displayName,
            reps = last?.reps?.toString() ?: ex.suggestedReps.orEmpty(),
            weightKg = formatKg(last?.weightKg),
        )
    }

    private fun formatKg(kg: Float?): String {
        if (kg == null) return ""
        val v = kg
        return if (v == v.toInt().toFloat()) {
            v.toInt().toString()
        } else {
            v.toString()
        }
    }

    fun saveSession(onSaved: () -> Unit) {
        val state = _uiState.value
        val validSets = state.sets.filter { it.exerciseName.isNotBlank() }
        if (validSets.isEmpty()) {
            _uiState.update { it.copy(message = "Isi minimal 1 latihan dulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            try {
                val now = System.currentTimeMillis()
                val zone = ZoneId.systemDefault()
                val sessionDay = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
                val durationMinutes = ((now - sessionStartedAtMs) / 60_000L).toInt()

                val sessionId = sessionDao.insert(
                    WorkoutSessionEntity(
                        startedAtEpochMs = sessionStartedAtMs,
                        endedAtEpochMs = now,
                        type = state.type,
                        notes = state.notes.takeIf { it.isNotBlank() },
                    ),
                )

                val setEntities = validSets.mapIndexed { idx, set ->
                    ExerciseSetEntity(
                        sessionId = sessionId,
                        exerciseName = set.exerciseName.trim(),
                        setIndex = idx + 1,
                        reps = set.reps.toIntOrNull(),
                        weightKg = set.weightKg.toFloatOrNull(),
                    )
                }
                setDao.insertAll(setEntities)

                val prev = playerStateDao.getRow()
                val lastDate = prev?.lastSessionLocalDate?.let { LocalDate.parse(it) }
                val newStreak = ProgressRules.nextStreak(
                    lastDate,
                    prev?.streakDays ?: 0,
                    sessionDay,
                )
                val xpGain = ProgressRules.sessionXpGain(durationMinutes, newStreak)
                val newXp = (prev?.xpTotal ?: 0) + xpGain
                val newRank = ProgressRules.rankFromLevel(ProgressRules.levelFromXp(newXp))
                playerStateDao.upsert(
                    PlayerStateEntity(
                        id = 1,
                        xpTotal = newXp,
                        streakDays = newStreak,
                        lastSessionLocalDate = sessionDay.toString(),
                        rankCode = newRank,
                    ),
                )

                bumpDailySessionQuest(sessionDay)

                _uiState.update {
                    it.copy(message = "Sesi berhasil disimpan. +$xpGain XP")
                }
                delay(320)
                onSaved()
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun bumpDailySessionQuest(sessionDay: LocalDate) {
        val id = DailyQuestTemplates.todaySessionQuestId(sessionDay)
        val existing = questDao.getById(id)
        val base = existing ?: DailyQuestTemplates.todaySessionQuest(sessionDay)
        val nextProgress = (base.progressValue + 1).coerceAtMost(base.targetValue)
        val status = if (nextProgress >= base.targetValue) "completed" else "active"
        questDao.upsert(
            base.copy(
                progressValue = nextProgress,
                status = status,
            ),
        )
    }
}
