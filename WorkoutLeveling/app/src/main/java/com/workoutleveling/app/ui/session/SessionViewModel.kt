package com.workoutleveling.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.ExerciseCatalogEntity
import com.workoutleveling.app.data.local.db.ExerciseCatalogSeed
import com.workoutleveling.app.data.local.db.ExerciseSetEntity
import com.workoutleveling.app.data.local.db.PlayerStateEntity
import com.workoutleveling.app.data.local.db.WorkoutSessionEntity
import com.workoutleveling.app.domain.catalog.CatalogExercise
import com.workoutleveling.app.domain.catalog.WorkoutCatalog
import com.workoutleveling.app.domain.progress.DailyQuestTemplates
import com.workoutleveling.app.domain.progress.PlayerRankSync
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.domain.progress.WeeklyQuestTemplates
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

enum class AddCustomCatalogResult {
    Added,
    AlreadyExists,
    InvalidName,
}

data class DraftExerciseSet(
    val rowId: Long,
    val exerciseName: String = "",
    val reps: String = "",
    val weightKg: String = "",
    /** Detik (opsional), untuk cardio / mesin. */
    val durationSec: String = "",
    /** Catat jika ada nyeri tajam saat gerakan ini. */
    val sharpPain: Boolean = false,
)

data class SessionUiState(
    val type: String = "A",
    val guidedStarted: Boolean = false,
    val currentExerciseIndex: Int = 0,
    val recommendedPlanLabel: String = "Day A",
    val coachCurrentSet: Int = 1,
    val coachTargetSets: Int = 3,
    val coachLoadMode: String = "Normal",
    val readyCountdownSec: Int = 0,
    val workoutLive: Boolean = false,
    val workoutPaused: Boolean = false,
    val activeSetElapsedSec: Int = 0,
    val restSecondsLeft: Int = 0,
    val restRunning: Boolean = false,
    val notes: String = "",
    /** Usaha subjektif 1–10 (opsional), disimpan ke `effortRpe`. */
    val effortRpe: String = "",
    /** Mobility / cooldown ringan di akhir sesi (+XP jika dicentang). */
    val includedRecoveryMobility: Boolean = false,
    val sleepQuality: String = "3",
    val energyLevel: String = "3",
    val sorenessLevel: String = "3",
    val sets: List<DraftExerciseSet> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WorkoutLevelingApp
    private val db = app.database
    private val sessionDao = db.workoutSessionDao()
    private val setDao = db.exerciseSetDao()
    private val playerStateDao = db.playerStateDao()
    private val questDao = db.questDao()
    private val catalogDao = db.exerciseCatalogDao()
    private val painDao = db.painReportDao()

    private val sessionStartedAtMs: Long = System.currentTimeMillis()

    private var nextDraftRowId = 1L
    private var restJob: Job? = null
    private var readyJob: Job? = null
    private var setTickerJob: Job? = null
    private val cardioFinisherName = "Cardio finisher (Zona 2)"

    private fun newDraft(
        exerciseName: String = "",
        reps: String = "",
        weightKg: String = "",
        durationSec: String = "",
    ) = DraftExerciseSet(
        rowId = nextDraftRowId++,
        exerciseName = exerciseName,
        reps = reps,
        weightKg = weightKg,
        durationSec = durationSec,
    )

    private fun isCardioStepName(name: String): Boolean {
        val n = name.lowercase()
        return n.contains("cardio") || n.contains("jalan") || n.contains("jog")
    }

    private val _uiState = MutableStateFlow(SessionUiState(sets = listOf(newDraft())))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    val coachSoundEnabled: StateFlow<Boolean> = app.userPreferences.isCoachSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    init {
        viewModelScope.launch { prepareRecommendedPlan() }
    }

    private fun normalizeReadiness(raw: String): String {
        val v = raw.filter(Char::isDigit).take(1).toIntOrNull() ?: 3
        return v.coerceIn(1, 5).toString()
    }

    fun setType(type: String) {
        _uiState.update { it.copy(type = type) }
    }

    fun setCoachLoadMode(mode: String) {
        val safe = when (mode) {
            "Ringan", "Berat" -> mode
            else -> "Normal"
        }
        _uiState.update { it.copy(coachLoadMode = safe) }
    }

    fun setCoachSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setCoachSoundEnabled(enabled) }
    }

    fun startGuidedSession() {
        stopReady()
        stopRest()
        stopSetTicker()
        _uiState.update {
            it.copy(
                guidedStarted = true,
                readyCountdownSec = 3,
                workoutLive = false,
                workoutPaused = false,
                activeSetElapsedSec = 0,
                coachCurrentSet = 1,
                restRunning = false,
                restSecondsLeft = 0,
            )
        }
        readyJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val latest = _uiState.value
                val next = (latest.readyCountdownSec - 1).coerceAtLeast(0)
                _uiState.update { it.copy(readyCountdownSec = next) }
                if (next == 0) {
                    _uiState.update { it.copy(workoutLive = true, workoutPaused = false) }
                    startSetTicker()
                    break
                }
            }
        }
    }

    fun nextExercise() {
        stopRest()
        stopReady()
        stopSetTicker()
        _uiState.update { state ->
            val next = (state.currentExerciseIndex + 1).coerceAtMost(state.sets.lastIndex.coerceAtLeast(0))
            state.copy(
                currentExerciseIndex = next,
                coachCurrentSet = 1,
                coachTargetSets = coachTargetSetsForIndex(state, next),
                readyCountdownSec = 0,
                workoutLive = true,
                workoutPaused = false,
                activeSetElapsedSec = 0,
                restSecondsLeft = 0,
                restRunning = false,
            )
        }
        startSetTicker()
    }

    fun previousExercise() {
        stopRest()
        stopReady()
        stopSetTicker()
        _uiState.update { state ->
            val prev = (state.currentExerciseIndex - 1).coerceAtLeast(0)
            state.copy(
                currentExerciseIndex = prev,
                coachCurrentSet = 1,
                coachTargetSets = coachTargetSetsForIndex(state, prev),
                readyCountdownSec = 0,
                workoutLive = true,
                workoutPaused = false,
                activeSetElapsedSec = 0,
                restSecondsLeft = 0,
                restRunning = false,
            )
        }
        startSetTicker()
    }

    fun completeCurrentSetAndStartRest() {
        val state = _uiState.value
        if (state.restRunning) return
        if (isCurrentStepCardio(state)) return
        if (!state.workoutLive) return
        val nextSet = (state.coachCurrentSet + 1).coerceAtMost(state.coachTargetSets)
        stopSetTicker()
        _uiState.update {
            it.copy(
                coachCurrentSet = nextSet,
                activeSetElapsedSec = 0,
                restRunning = nextSet < it.coachTargetSets,
                restSecondsLeft = if (nextSet < it.coachTargetSets) 90 else 0,
            )
        }
        if (nextSet < state.coachTargetSets) {
            startRestCountdown()
        } else {
            _uiState.update { it.copy(workoutLive = false, workoutPaused = false) }
        }
    }

    fun skipRest() {
        stopRest()
        _uiState.update { it.copy(restRunning = false, restSecondsLeft = 0, workoutLive = true, workoutPaused = false) }
        startSetTicker()
    }

    fun togglePause() {
        _uiState.update { state ->
            if (!state.workoutLive && !state.restRunning) return@update state
            state.copy(workoutPaused = !state.workoutPaused)
        }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun setEffortRpe(value: String) {
        val digits = value.filter(Char::isDigit).take(2)
        _uiState.update { it.copy(effortRpe = digits) }
    }

    fun setIncludedRecoveryMobility(value: Boolean) {
        _uiState.update { it.copy(includedRecoveryMobility = value) }
    }

    fun setSleepQuality(value: String) {
        _uiState.update { it.copy(sleepQuality = normalizeReadiness(value)) }
    }

    fun setEnergyLevel(value: String) {
        _uiState.update { it.copy(energyLevel = normalizeReadiness(value)) }
    }

    fun setSorenessLevel(value: String) {
        _uiState.update { it.copy(sorenessLevel = normalizeReadiness(value)) }
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

    /** Gabungan katalog Room + nama yang pernah dilog; filter HG60 opsional. */
    suspend fun autocompleteExerciseNames(rawQuery: String, hg60Only: Boolean): List<String> =
        withContext(Dispatchers.IO) {
            ExerciseCatalogSeed.ensureSeeded(catalogDao)
            val q = rawQuery.trim()
            val fromCat = catalogDao.searchCatalog(q, hg60Only, 32)
            val fromLog = if (q.isNotEmpty()) {
                catalogDao.searchDistinctLoggedNames(q, 32)
            } else {
                emptyList()
            }
            val merged = LinkedHashSet<String>()
            fromCat.forEach { merged.add(it) }
            for (n in fromLog) {
                if (!hg60Only) {
                    merged.add(n)
                } else {
                    val entry = WorkoutCatalog.entryForName(n)
                    if (entry?.isHg60Station == true) merged.add(n)
                }
            }
            merged.toList()
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
                .take(12)
        }

    suspend fun isNameInExerciseCatalog(name: String): Boolean =
        withContext(Dispatchers.IO) {
            ExerciseCatalogSeed.ensureSeeded(catalogDao)
            val n = name.trim()
            if (n.isEmpty()) return@withContext false
            catalogDao.countByNameIgnoreCase(n) > 0
        }

    suspend fun addCustomExerciseToCatalog(name: String, isHg60: Boolean): AddCustomCatalogResult =
        withContext(Dispatchers.IO) {
            ExerciseCatalogSeed.ensureSeeded(catalogDao)
            val n = name.trim()
            if (n.isEmpty()) return@withContext AddCustomCatalogResult.InvalidName
            val nextOrder = catalogDao.maxSortOrder() + 1
            val tag = if (isHg60) "CUSTOM_HG60" else "CUSTOM"
            val rowId = catalogDao.insert(
                ExerciseCatalogEntity(
                    displayName = n,
                    equipmentTag = tag,
                    isHg60Station = isHg60,
                    sortOrder = nextOrder,
                ),
            )
            if (rowId == -1L) AddCustomCatalogResult.AlreadyExists
            else AddCustomCatalogResult.Added
        }

    /** True jika ada isian yang hilang jika keluar tanpa simpan. */
    fun isDraftDirty(): Boolean {
        val s = _uiState.value
        if (s.notes.isNotBlank()) return true
        if (s.effortRpe.isNotBlank()) return true
        if (s.includedRecoveryMobility) return true
        if (s.sleepQuality != "3" || s.energyLevel != "3" || s.sorenessLevel != "3") return true
        if (s.type != "A") return true
        if (s.sets.size != 1) return true
        return s.sets.any { row ->
            row.exerciseName.isNotBlank() || row.reps.isNotBlank() ||
                row.weightKg.isNotBlank() || row.durationSec.isNotBlank()
        }
    }

    fun applyTemplateDayA() {
        applyTemplate("A", WorkoutCatalog.dayA, "Template Day A diterapkan.", "Day A")
    }

    fun applyTemplateDayB() {
        applyTemplate("B", WorkoutCatalog.dayB, "Template Day B diterapkan.", "Day B")
    }

    fun applyTemplateCardio() {
        applyTemplate("Cardio", WorkoutCatalog.cardio, "Template cardio diterapkan.", "Cardio")
    }

    private fun applyTemplate(type: String, items: List<CatalogExercise>, msg: String, planLabel: String) {
        viewModelScope.launch {
            stopRest()
            val baseDrafts = items.map { ex -> draftFromCatalog(ex) }
            val drafts = when (type) {
                "A", "B" -> {
                    val finisher = newDraft(
                        exerciseName = cardioFinisherName,
                        durationSec = "600",
                    )
                    baseDrafts + finisher
                }
                "Cardio" -> {
                    baseDrafts.map { d ->
                        if (isCardioStepName(d.exerciseName) && d.durationSec.isBlank()) {
                            d.copy(durationSec = "1800")
                        } else {
                            d
                        }
                    }
                }
                else -> baseDrafts
            }
            _uiState.update {
                it.copy(
                    type = type,
                    guidedStarted = false,
                    currentExerciseIndex = 0,
                    recommendedPlanLabel = planLabel,
                    coachCurrentSet = 1,
                    coachTargetSets = coachTargetSetsForDraft(drafts.firstOrNull()),
                    readyCountdownSec = 0,
                    workoutLive = false,
                    workoutPaused = false,
                    activeSetElapsedSec = 0,
                    restRunning = false,
                    restSecondsLeft = 0,
                    sets = drafts,
                    message = msg,
                )
            }
        }
    }

    private suspend fun prepareRecommendedPlan() {
        val latest = sessionDao.getLatestTypeOrNull()
        when (latest) {
            "A" -> applyTemplate("B", WorkoutCatalog.dayB, "Rencana hari ini: Day B (otomatis).", "Day B")
            "B" -> applyTemplate("Cardio", WorkoutCatalog.cardio, "Rencana hari ini: Cardio (otomatis).", "Cardio")
            "Cardio" -> applyTemplate("A", WorkoutCatalog.dayA, "Rencana hari ini: Day A (otomatis).", "Day A")
            else -> applyTemplate("A", WorkoutCatalog.dayA, "Rencana hari ini: Day A (otomatis).", "Day A")
        }
    }

    /** Dipanggil saat user memilih nama dari menu katalog. */
    fun applyPickedExercise(index: Int, name: String) {
        viewModelScope.launch {
            val last = setDao.getLatestSetForExerciseName(name)
            val sug = WorkoutCatalog.suggestedRepsForName(name)
            val lastTwo = setDao.getLatestTwoSetsForExerciseName(name)
            _uiState.update { state ->
                val next = state.sets.mapIndexed { i, set ->
                    if (i != index) return@mapIndexed set
                    set.copy(
                        exerciseName = name,
                        reps = last?.reps?.toString() ?: sug.orEmpty(),
                        weightKg = formatKg(adjustWeightByLoad(last?.weightKg, state.coachLoadMode)),
                    )
                }
                val target = sug?.toIntOrNull()
                val hitTopTwice = if (target != null && lastTwo.size >= 2) {
                    lastTwo.all { (it.reps ?: 0) >= target }
                } else {
                    false
                }
                val msg = if (hitTopTwice) {
                    "Progression: 2 sesi terakhir sudah capai batas reps, pertimbangkan naik beban kecil."
                } else {
                    null
                }
                state.copy(sets = next, message = msg)
            }
        }
    }

    private suspend fun draftFromCatalog(ex: CatalogExercise): DraftExerciseSet {
        val last = setDao.getLatestSetForExerciseName(ex.displayName)
        return newDraft(
            exerciseName = ex.displayName,
            reps = last?.reps?.toString() ?: ex.suggestedReps.orEmpty(),
            weightKg = formatKg(last?.weightKg),
            durationSec = last?.durationSec?.toString().orEmpty(),
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

    private fun adjustWeightByLoad(baseKg: Float?, mode: String): Float? {
        if (baseKg == null) return null
        val adjusted = when (mode) {
            "Ringan" -> baseKg - 2.5f
            "Berat" -> baseKg + 2.5f
            else -> baseKg
        }
        return adjusted.coerceAtLeast(0f)
    }

    fun saveSession(onSaved: () -> Unit) {
        stopReady()
        stopRest()
        stopSetTicker()
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

                val rpe = state.effortRpe.toIntOrNull()?.coerceIn(1, 10)
                val sessionId = sessionDao.insert(
                    WorkoutSessionEntity(
                        startedAtEpochMs = sessionStartedAtMs,
                        endedAtEpochMs = now,
                        type = state.type,
                        notes = state.notes.takeIf { it.isNotBlank() },
                        effortRpe = rpe,
                        includedRecoveryMobility = state.includedRecoveryMobility,
                    ),
                )

                val setEntities = validSets.mapIndexed { idx, set ->
                    ExerciseSetEntity(
                        sessionId = sessionId,
                        exerciseName = set.exerciseName.trim(),
                        setIndex = idx + 1,
                        reps = set.reps.toIntOrNull(),
                        weightKg = set.weightKg.toFloatOrNull(),
                        durationSec = set.durationSec.toIntOrNull(),
                    )
                }
                setDao.insertAll(setEntities)
                val painfulExercises = validSets.filter { it.sharpPain }.map { it.exerciseName.trim() }.distinct()
                if (painfulExercises.isNotEmpty()) {
                    painDao.insertAll(
                        painfulExercises.map {
                            com.workoutleveling.app.data.local.db.PainReportEntity(
                                exerciseName = it,
                                reportedAtEpochMs = now,
                            )
                        },
                    )
                }

                val prev = playerStateDao.getRow()
                val lastDate = prev?.lastSessionLocalDate?.let { LocalDate.parse(it) }
                val newStreak = ProgressRules.nextStreak(
                    lastDate,
                    prev?.streakDays ?: 0,
                    sessionDay,
                )
                val xpGain = ProgressRules.sessionXpGain(
                    durationMinutes,
                    newStreak,
                    state.includedRecoveryMobility,
                )
                val newXp = (prev?.xpTotal ?: 0) + xpGain
                val prevRank = prev?.rankCode ?: "E"
                playerStateDao.upsert(
                    PlayerStateEntity(
                        id = 1,
                        xpTotal = newXp,
                        streakDays = newStreak,
                        lastSessionLocalDate = sessionDay.toString(),
                        rankCode = prevRank,
                    ),
                )
                PlayerRankSync.refreshRankFromHistory(sessionDao, playerStateDao)

                bumpDailySessionQuest(sessionDay)
                bumpWeeklySessionQuest(sessionDay)

                val readinessLow = (state.sleepQuality.toIntOrNull() ?: 3) <= 2 ||
                    (state.energyLevel.toIntOrNull() ?: 3) <= 2 ||
                    (state.sorenessLevel.toIntOrNull() ?: 3) >= 4
                val painWindowStart = now - 30L * 24 * 60 * 60 * 1000
                val painWarning = painfulExercises.firstOrNull { ex ->
                    painDao.countRecentForExercise(ex, painWindowStart) >= 2
                }
                val tail = buildString {
                    if (readinessLow) append(" Readiness rendah: pertimbangkan sesi lebih ringan besok.")
                    if (painWarning != null) append(" Nyeri berulang di \"$painWarning\": ganti variasi/kurangi beban.")
                }
                _uiState.update { it.copy(message = "Sesi berhasil disimpan. +$xpGain XP$tail") }
                delay(320)
                onSaved()
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun coachTargetSetsForIndex(state: SessionUiState, index: Int): Int {
        val step = state.sets.getOrNull(index)
        return coachTargetSetsForDraft(step)
    }

    private fun coachTargetSetsForDraft(step: DraftExerciseSet?): Int {
        if (step == null) return 3
        return if (isCardioStepName(step.exerciseName) || step.durationSec.isNotBlank()) 1 else 3
    }

    private fun isCurrentStepCardio(state: SessionUiState): Boolean {
        val step = state.sets.getOrNull(state.currentExerciseIndex) ?: return false
        return coachTargetSetsForDraft(step) == 1
    }

    private fun startRestCountdown() {
        restJob?.cancel()
        restJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val latest = _uiState.value
                if (!latest.restRunning) break
                if (latest.workoutPaused) continue
                val next = (latest.restSecondsLeft - 1).coerceAtLeast(0)
                _uiState.update { it.copy(restSecondsLeft = next) }
                if (next == 0) {
                    _uiState.update { it.copy(restRunning = false, workoutLive = true, workoutPaused = false) }
                    startSetTicker()
                    break
                }
            }
        }
    }

    private fun stopRest() {
        restJob?.cancel()
        restJob = null
    }

    private fun startSetTicker() {
        setTickerJob?.cancel()
        setTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val latest = _uiState.value
                if (!latest.workoutLive || latest.workoutPaused || latest.restRunning) continue
                _uiState.update { it.copy(activeSetElapsedSec = it.activeSetElapsedSec + 1) }
            }
        }
    }

    private fun stopSetTicker() {
        setTickerJob?.cancel()
        setTickerJob = null
    }

    private fun stopReady() {
        readyJob?.cancel()
        readyJob = null
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

    private suspend fun bumpWeeklySessionQuest(sessionDay: LocalDate) {
        val weekKey = WeeklyQuestTemplates.isoWeekPeriodKey(sessionDay)
        val id = WeeklyQuestTemplates.weeklySessionsQuestId(weekKey)
        val existing = questDao.getById(id)
        val base = existing ?: WeeklyQuestTemplates.weeklySessionsQuest(weekKey)
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
