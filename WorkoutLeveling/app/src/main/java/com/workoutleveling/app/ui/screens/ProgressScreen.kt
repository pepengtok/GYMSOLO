@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.data.local.db.DatabaseImportResult
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.ui.components.CardioMinutesTrendChart
import com.workoutleveling.app.ui.components.AssetImage
import com.workoutleveling.app.ui.components.HealthConnectStepsCard
import com.workoutleveling.app.ui.components.SessionTrendChart
import com.workoutleveling.app.ui.components.VolumeTrendChart
import com.workoutleveling.app.ui.progress.ProgressViewModel
import kotlinx.coroutines.launch

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
    onOpenBodyCheckIn: () -> Unit,
    onOpenCustomCatalog: () -> Unit,
) {
    val player by viewModel.player.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val questSummary by viewModel.summaryText.collectAsStateWithLifecycle()
    val sessionStats by viewModel.sessionStats.collectAsStateWithLifecycle()
    val calories by viewModel.calories.collectAsStateWithLifecycle()
    val calorieProfile by viewModel.calorieProfile.collectAsStateWithLifecycle()
    val cut by viewModel.cutProgress.collectAsStateWithLifecycle()
    val trend by viewModel.last14DaysTrend.collectAsStateWithLifecycle()
    val volumeTrend by viewModel.last14DaysVolumeTrend.collectAsStateWithLifecycle()
    val cardioTrend by viewModel.last14DaysCardioMinutesTrend.collectAsStateWithLifecycle()
    val importing by viewModel.importInProgress.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as ComponentActivity

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            when (val r = viewModel.importDatabase(uri)) {
                DatabaseImportResult.Success -> activity.recreate()
                is DatabaseImportResult.Failure ->
                    snackbarHostState.showSnackbar(r.userMessage)
            }
        }
    }

    val levelProgress = (player.xpIntoLevel.toFloat() / ProgressRules.XP_PER_LEVEL).coerceIn(0f, 1f)
    var profileWeight by remember { mutableStateOf(calorieProfile.weightKg.toString()) }
    var profileHeight by remember { mutableStateOf(calorieProfile.heightCm.toString()) }
    var profileAge by remember { mutableStateOf(calorieProfile.ageYears.toString()) }
    var profileSex by remember { mutableStateOf(calorieProfile.sex) }
    var profileTarget by remember { mutableStateOf(calorieProfile.weeklyTargetKcal.toString()) }
    LaunchedEffect(calorieProfile) {
        profileWeight = calorieProfile.weightKg.toString()
        profileHeight = calorieProfile.heightCm.toString()
        profileAge = calorieProfile.ageYears.toString()
        profileSex = calorieProfile.sex
        profileTarget = calorieProfile.weeklyTargetKcal.toString()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            AssetImage(
                assetPath = "image/illu_gate_clear.png",
                contentDescription = "Ilustrasi Progress",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
            Text("Status pemain", style = MaterialTheme.typography.titleLarge)
            Text(
                "Level ${player.level} · Rank ${player.rank} · Streak ${player.streak} hari",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Total XP: ${player.xp}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Menuju level berikutnya: ${player.xpToNext} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            Text(
                "${player.xpIntoLevel} / ${ProgressRules.XP_PER_LEVEL} XP di level ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 6.dp),
            )

            Text(
                "Ringkasan sesi",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 28.dp),
            )
            Text(
                "Total sesi tercatat: ${sessionStats.totalSessions}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Sesi minggu ini (Sen–Min, ISO): ${sessionStats.sessionsThisIsoWeek}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Cardio minggu ini: ${sessionStats.cardioMinutesThisIsoWeek} menit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Kalori terbakar: hari ini ${calories.kcalToday} kkal · minggu ini ${calories.kcalThisIsoWeek} kkal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 4.dp),
            )
            WeeklyGoalBar(
                label = "Strength",
                value = sessionStats.strengthSessionsThisIsoWeek,
                target = viewModel.targetStrengthSessions,
                modifier = Modifier.padding(top = 8.dp),
            )
            WeeklyGoalBar(
                label = "Cardio sesi",
                value = sessionStats.cardioSessionsThisIsoWeek,
                target = viewModel.targetCardioSessions,
                modifier = Modifier.padding(top = 6.dp),
            )
            WeeklyGoalBar(
                label = "Mobility",
                value = sessionStats.mobilitySessionsThisIsoWeek,
                target = viewModel.targetMobilitySessions,
                modifier = Modifier.padding(top = 6.dp),
            )
            WeeklyGoalBar(
                label = "Cardio menit",
                value = sessionStats.cardioMinutesThisIsoWeek,
                target = viewModel.targetCardioMinutesWeekly,
                modifier = Modifier.padding(top = 6.dp),
            )
            WeeklyGoalBar(
                label = "Kalori mingguan",
                value = calories.kcalThisIsoWeek,
                target = viewModel.targetCaloriesWeekly,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "Profil kalori (personal)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 14.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = profileWeight,
                    onValueChange = { profileWeight = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Berat kg") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = profileHeight,
                    onValueChange = { profileHeight = it.filter(Char::isDigit) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Tinggi cm") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = profileAge,
                    onValueChange = { profileAge = it.filter(Char::isDigit) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Usia") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = profileSex,
                    onValueChange = { profileSex = it.lowercase() },
                    modifier = Modifier.weight(1f),
                    label = { Text("Sex: male/female") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = profileTarget,
                onValueChange = { profileTarget = it.filter(Char::isDigit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Target kalori mingguan") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedButton(
                onClick = {
                    viewModel.saveCalorieProfile(
                        weightKg = profileWeight,
                        heightCm = profileHeight,
                        ageYears = profileAge,
                        sex = profileSex,
                        weeklyTargetKcal = profileTarget,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("Simpan profil kalori")
            }
            Text(
                "Zona 2: pace masih bisa ngomong kalimat pendek.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Akumulasi 30 hari: ${calories.kcalLast30Days} kkal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                modifier = Modifier.padding(top = 3.dp),
            )
            if (viewModel.deloadHint.isNotBlank()) {
                Text(
                    viewModel.deloadHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            OutlinedButton(
                onClick = onOpenBodyCheckIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("Check-in berat & pinggang")
            }
            OutlinedButton(
                onClick = onOpenCustomCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("Kelola katalog custom")
            }

            Text(
                "Tren sesi",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 28.dp),
            )
            SessionTrendChart(
                points = trend,
                modifier = Modifier.padding(top = 8.dp),
            )

            VolumeTrendChart(
                points = volumeTrend,
                modifier = Modifier.padding(top = 20.dp),
            )
            CardioMinutesTrendChart(
                points = cardioTrend,
                modifier = Modifier.padding(top = 20.dp),
            )

            HealthConnectStepsCard(modifier = Modifier.padding(top = 24.dp))

            if (importing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                )
                Text(
                    "Mengimpor backup…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            OutlinedButton(
                onClick = { viewModel.shareDatabaseBackup() },
                enabled = !importing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("Ekspor backup database")
            }
            OutlinedButton(
                onClick = {
                    importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                },
                enabled = !importing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("Impor backup database…")
            }
            Text(
                "Impor menimpa data lokal. Hanya file SQLite backup dari app ini (ekspor di atas); versi schema lebih baru ditolak.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 6.dp),
            )

            Row(
                modifier = Modifier.padding(top = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssetImage(
                    assetPath = "image/icon_quest.png",
                    contentDescription = "Quest",
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "Quest Harian & Mingguan",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                "Cut trend (data check-in terbaru): berat Δ ${
                    cut.weightDeltaKg?.let { String.format("%.1fkg", it) } ?: "-"
                } · pinggang Δ ${cut.waistDeltaCm?.let { String.format("%.1fcm", it) } ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                questSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp),
            )
            if (quests.isEmpty()) {
                AssetImage(
                    assetPath = "image/empty_no_quests.png.png",
                    contentDescription = "Belum ada quest",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentScale = ContentScale.FillWidth,
                )
                Text(
                    "Belum ada quest aktif. Buka app setiap hari dan selesaikan sesi untuk memulai progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                quests.forEach { q ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (q.questType == "weekly") "Mingguan · ${q.title}" else "Harian · ${q.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    "${q.progressValue} / ${q.targetValue}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Text(
                                if (q.status == "completed") "Selesai" else "Aktif",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (q.status == "completed") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                },
                            )
                        }
                    }
                }
            }

            Text(
                "Rumus XP: 50 dasar + menit latihan (maks 45) + bonus streak (>=3 hari) + mobility/cooldown (+10). Rank dihitung dari aktivitas 4 minggu terakhir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun WeeklyGoalBar(
    label: String,
    value: Int,
    target: Int,
    modifier: Modifier = Modifier,
) {
    val safeTarget = target.coerceAtLeast(1)
    val p by animateFloatAsState(
        targetValue = (value.toFloat() / safeTarget).coerceIn(0f, 1f),
        label = "weekly_goal_progress",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
            Text(
                "$value/$target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
        LinearProgressIndicator(
            progress = { p },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
        )
    }
}
