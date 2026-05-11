package com.workoutleveling.app.ui.progress

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.export.DatabaseExport
import com.workoutleveling.app.data.local.db.DatabaseImportResult
import com.workoutleveling.app.data.local.db.BodyMetricsEntity
import com.workoutleveling.app.data.local.db.PlayerStateEntity
import com.workoutleveling.app.data.local.db.QuestEntity
import com.workoutleveling.app.domain.progress.CalorieEstimator
import com.workoutleveling.app.domain.progress.PlayerRankSync
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.domain.progress.QuestBootstrap
import com.workoutleveling.app.domain.progress.WeeklyQuestTemplates
import com.workoutleveling.app.domain.time.WeekBounds
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerProgressUi(
    val xp: Int,
    val level: Int,
    val rank: String,
    val streak: Int,
    val xpIntoLevel: Int,
    val xpToNext: Int,
)

data class SessionStatsUi(
    val totalSessions: Int,
    val sessionsThisIsoWeek: Int,
    val cardioSessionsThisIsoWeek: Int,
    val cardioMinutesThisIsoWeek: Int,
    val strengthSessionsThisIsoWeek: Int,
    val mobilitySessionsThisIsoWeek: Int,
)

data class TrendDayUi(val label: String, val count: Int)

data class TrendVolumeDayUi(val label: String, val volumeKg: Float)
data class TrendCardioMinutesDayUi(val label: String, val minutes: Int)
data class CalorieSummaryUi(
    val kcalToday: Int,
    val kcalThisIsoWeek: Int,
    val kcalLast30Days: Int,
)
data class CalorieProfileUi(
    val weightKg: Float,
    val heightCm: Int,
    val ageYears: Int,
    val sex: String,
    val weeklyTargetKcal: Int,
)

data class CutProgressUi(
    val weightDeltaKg: Float?,
    val waistDeltaCm: Float?,
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val _importInProgress = MutableStateFlow(false)
    val importInProgress: StateFlow<Boolean> = _importInProgress.asStateFlow()

    private val app = application as WorkoutLevelingApp
    private val playerDao get() = app.database.playerStateDao()
    private val questDao get() = app.database.questDao()
    private val sessionDao get() = app.database.workoutSessionDao()
    private val exerciseSetDao get() = app.database.exerciseSetDao()
    private val bodyMetricsDao get() = app.database.bodyMetricsDao()

    val targetStrengthSessions = 3
    val targetCardioSessions = 2
    val targetMobilitySessions = 1
    val targetCardioMinutesWeekly = 120
    val calorieProfile: StateFlow<CalorieProfileUi> =
        combine(
            app.userPreferences.calorieProfileWeightKg,
            app.userPreferences.calorieProfileHeightCm,
            app.userPreferences.calorieProfileAgeYears,
            app.userPreferences.calorieProfileSex,
            app.userPreferences.calorieWeeklyTarget,
        ) { w, h, a, s, t ->
            CalorieProfileUi(
                weightKg = w,
                heightCm = h,
                ageYears = a,
                sex = s,
                weeklyTargetKcal = t,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CalorieProfileUi(75f, 170, 30, "unspecified", 1800),
        )

    val targetCaloriesWeekly: Int
        get() = calorieProfile.value.weeklyTargetKcal


    private val todayKey = LocalDate.now().toString()
    private val weekKey = WeeklyQuestTemplates.isoWeekPeriodKey(LocalDate.now())

    init {
        viewModelScope.launch {
            QuestBootstrap.ensureTodayAndWeek(questDao)
            PlayerRankSync.refreshRankFromHistory(sessionDao, playerDao)
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

    val quests: StateFlow<List<QuestEntity>> =
        questDao.observeForDayAndWeek(todayKey, weekKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionStats: StateFlow<SessionStatsUi> =
        combine(sessionDao.observeAll(), exerciseSetDao.observeAll()) { sessions, sets ->
                val (wStart, wEnd) = WeekBounds.currentIsoWeekRangeMillis()
                val weekCount = sessions.count { it.startedAtEpochMs >= wStart && it.startedAtEpochMs < wEnd }
                val strengthCount = sessions.count {
                    it.startedAtEpochMs in wStart until wEnd && (it.type == "A" || it.type == "B")
                }
                val cardioCount = sessions.count {
                    it.startedAtEpochMs in wStart until wEnd && it.type == "Cardio"
                }
                val mobilityCount = sessions.count {
                    it.startedAtEpochMs in wStart until wEnd && it.includedRecoveryMobility
                }
                val setsBySession = sets.groupBy { it.sessionId }
                val cardioMinutes = sessions
                    .filter { it.type == "Cardio" && it.startedAtEpochMs in wStart until wEnd }
                    .sumOf { sess ->
                        val fromSetsSec = setsBySession[sess.id].orEmpty().sumOf { it.durationSec ?: 0 }
                        if (fromSetsSec > 0) {
                            fromSetsSec / 60
                        } else {
                            val end = sess.endedAtEpochMs ?: sess.startedAtEpochMs
                            ((end - sess.startedAtEpochMs) / 60_000L).toInt().coerceAtLeast(0)
                        }
                    }
                SessionStatsUi(
                    totalSessions = sessions.size,
                    sessionsThisIsoWeek = weekCount,
                    cardioSessionsThisIsoWeek = cardioCount,
                    cardioMinutesThisIsoWeek = cardioMinutes,
                    strengthSessionsThisIsoWeek = strengthCount,
                    mobilitySessionsThisIsoWeek = mobilityCount,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SessionStatsUi(0, 0, 0, 0, 0, 0),
            )

    val last14DaysTrend: StateFlow<List<TrendDayUi>> =
        sessionDao.observeAll()
            .map { sessions ->
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                (0 until 14).map { i ->
                    val day = today.minusDays((13 - i).toLong())
                    val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
                    val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    val c = sessions.count { it.startedAtEpochMs >= start && it.startedAtEpochMs < end }
                    TrendDayUi("${day.dayOfMonth}/${day.monthValue}", c)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val last14DaysVolumeTrend: StateFlow<List<TrendVolumeDayUi>> =
        combine(sessionDao.observeAll(), exerciseSetDao.observeAll()) { sessions, sets ->
            val bySessionStart = sessions.associate { it.id to it.startedAtEpochMs }
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            (0 until 14).map { i ->
                val day = today.minusDays((13 - i).toLong())
                val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                var vol = 0.0
                for (s in sets) {
                    val t = bySessionStart[s.sessionId] ?: continue
                    if (t < start || t >= end) continue
                    val r = s.reps ?: 0
                    val w = s.weightKg ?: 0f
                    vol += r * w.toDouble()
                }
                TrendVolumeDayUi("${day.dayOfMonth}/${day.monthValue}", vol.toFloat())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cutProgress: StateFlow<CutProgressUi> =
        bodyMetricsDao.observeRecent(8)
            .map { rows ->
                val ordered = rows.sortedBy { it.recordedAtEpochMs }
                val first = ordered.firstOrNull()
                val last = ordered.lastOrNull()
                fun delta(sel: (BodyMetricsEntity) -> Float?): Float? {
                    val a = first?.let(sel)
                    val b = last?.let(sel)
                    return if (a != null && b != null) b - a else null
                }
                CutProgressUi(
                    weightDeltaKg = delta { it.weightKg },
                    waistDeltaCm = delta { it.waistCm },
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CutProgressUi(null, null))

    val calories: StateFlow<CalorieSummaryUi> =
        combine(
            sessionDao.observeAll(),
            exerciseSetDao.observeAll(),
            bodyMetricsDao.observeRecent(1),
            calorieProfile,
        ) { sessions, sets, metrics, profile ->
            val setsBySession = sets.groupBy { it.sessionId }
            val zone = ZoneId.systemDefault()
            val todayDate = LocalDate.now(zone)
            val todayStart = todayDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val todayEnd = todayDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val (weekStart, weekEnd) = WeekBounds.currentIsoWeekRangeMillis()
            val d30Start = todayDate.minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli()
            val bodyWeightKg = metrics.firstOrNull()?.weightKg?.takeIf { it > 0f } ?: profile.weightKg

            fun kcalForSession(sessId: Long): Int {
                val sess = sessions.firstOrNull { it.id == sessId } ?: return 0
                return estimateSessionCaloriesKcal(
                    sessionType = sess.type,
                    durationMinutes = sessionDurationMinutes(sess.startedAtEpochMs, sess.endedAtEpochMs, setsBySession[sess.id].orEmpty()),
                    setCount = setsBySession[sess.id].orEmpty().size,
                    includesMobility = sess.includedRecoveryMobility,
                    profile = profile.copy(weightKg = bodyWeightKg),
                )
            }

            val kcalToday = sessions
                .filter { it.startedAtEpochMs in todayStart until todayEnd }
                .sumOf { kcalForSession(it.id) }
            val kcalWeek = sessions
                .filter { it.startedAtEpochMs in weekStart until weekEnd }
                .sumOf { kcalForSession(it.id) }
            val kcal30 = sessions
                .filter { it.startedAtEpochMs >= d30Start }
                .sumOf { kcalForSession(it.id) }

            CalorieSummaryUi(
                kcalToday = kcalToday,
                kcalThisIsoWeek = kcalWeek,
                kcalLast30Days = kcal30,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CalorieSummaryUi(0, 0, 0),
        )

    val deloadHint: String
        get() {
            val week = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
            return if (week % 5 == 0) {
                "Minggu deload disarankan: turunkan volume 30-40% untuk recovery."
            } else {
                ""
            }
        }

    val last14DaysCardioMinutesTrend: StateFlow<List<TrendCardioMinutesDayUi>> =
        combine(sessionDao.observeAll(), exerciseSetDao.observeAll()) { sessions, sets ->
            val setsBySession = sets.groupBy { it.sessionId }
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            (0 until 14).map { i ->
                val day = today.minusDays((13 - i).toLong())
                val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val minutes = sessions
                    .filter { it.type == "Cardio" && it.startedAtEpochMs >= start && it.startedAtEpochMs < end }
                    .sumOf { sess ->
                        val fromSetsSec = setsBySession[sess.id].orEmpty().sumOf { it.durationSec ?: 0 }
                        if (fromSetsSec > 0) {
                            fromSetsSec / 60
                        } else {
                            val ended = sess.endedAtEpochMs ?: sess.startedAtEpochMs
                            ((ended - sess.startedAtEpochMs) / 60_000L).toInt().coerceAtLeast(0)
                        }
                    }
                TrendCardioMinutesDayUi("${day.dayOfMonth}/${day.monthValue}", minutes)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summaryText: StateFlow<String> =
        combine(player, quests) { _, list ->
            val daily = list.filter { it.questType == "daily" }
            val weekly = list.filter { it.questType == "weekly" }
            val dailyDone = daily.count { it.status == "completed" }
            val weeklyLine = weekly.firstOrNull()?.let { w ->
                "Mingguan: ${w.progressValue} / ${w.targetValue} sesi."
            }.orEmpty()
            val dailyLine = if (daily.isEmpty()) {
                ""
            } else {
                "Harian: $dailyDone / ${daily.size} selesai."
            }
            listOf(dailyLine, weeklyLine).filter { it.isNotBlank() }.joinToString(" ")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun shareDatabaseBackup() {
        val ctx = getApplication<Application>()
        val uri = DatabaseExport.prepareShareUri(ctx, app.database) ?: return
        val intent = Intent.createChooser(
            DatabaseExport.buildShareIntent(uri),
            "Bagikan backup database",
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        ctx.startActivity(intent)
    }

    suspend fun importDatabase(uri: Uri): DatabaseImportResult =
        withContext(Dispatchers.IO) {
            _importInProgress.value = true
            try {
                val ctx = getApplication<Application>()
                ctx.contentResolver.openInputStream(uri)?.use { input ->
                    (ctx as WorkoutLevelingApp).importDatabaseFromStream(input)
                } ?: DatabaseImportResult.Failure("Tidak bisa membuka file.")
            } catch (_: Exception) {
                DatabaseImportResult.Failure("Tidak bisa membaca file.")
            } finally {
                _importInProgress.value = false
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

    private fun sessionDurationMinutes(
        startedAtEpochMs: Long,
        endedAtEpochMs: Long?,
        sets: List<com.workoutleveling.app.data.local.db.ExerciseSetEntity>,
    ): Int {
        val secFromSets = sets.sumOf { it.durationSec ?: 0 }
        if (secFromSets > 0) return (secFromSets / 60).coerceAtLeast(0)
        val end = endedAtEpochMs ?: startedAtEpochMs
        return ((end - startedAtEpochMs) / 60_000L).toInt().coerceAtLeast(0)
    }

    private fun estimateSessionCaloriesKcal(
        sessionType: String,
        durationMinutes: Int,
        setCount: Int,
        includesMobility: Boolean,
        profile: CalorieProfileUi,
    ): Int {
        return CalorieEstimator.estimateSessionCaloriesKcal(
            sessionType = sessionType,
            durationMinutes = durationMinutes,
            setCount = setCount,
            includesMobility = includesMobility,
            profile = CalorieEstimator.Profile(
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                ageYears = profile.ageYears,
                sex = profile.sex,
            ),
        )
    }

    fun saveCalorieProfile(
        weightKg: String,
        heightCm: String,
        ageYears: String,
        sex: String,
        weeklyTargetKcal: String,
    ) {
        val w = weightKg.toFloatOrNull() ?: calorieProfile.value.weightKg
        val h = heightCm.toIntOrNull() ?: calorieProfile.value.heightCm
        val a = ageYears.toIntOrNull() ?: calorieProfile.value.ageYears
        val t = weeklyTargetKcal.toIntOrNull() ?: calorieProfile.value.weeklyTargetKcal
        viewModelScope.launch {
            app.userPreferences.setCalorieProfile(w, h, a, sex)
            app.userPreferences.setCalorieWeeklyTarget(t)
        }
    }
}
