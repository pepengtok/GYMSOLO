@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.workoutleveling.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.domain.catalog.CatalogExercise
import com.workoutleveling.app.domain.catalog.WorkoutCatalog
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.ui.components.AssetImage
import com.workoutleveling.app.ui.session.AddCustomCatalogResult
import com.workoutleveling.app.ui.session.CoachSoundPlayer
import com.workoutleveling.app.ui.session.DraftExerciseSet
import com.workoutleveling.app.ui.session.SessionViewModel
import java.util.Locale
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.launch

@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.coachSoundEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coachSound = remember { CoachSoundPlayer(context) }
    val immersiveActiveMode = uiState.guidedStarted
    val haptic = LocalHapticFeedback.current
    DisposableEffect(Unit) {
        onDispose { coachSound.release() }
    }
    val saveButtonRequester = remember { BringIntoViewRequester() }
    val templateScroll = rememberScrollState()
    val typeScroll = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var catalogHg60Only by rememberSaveable { mutableStateOf(false) }
    var showTemplate by rememberSaveable { mutableStateOf(true) }
    var showType by rememberSaveable { mutableStateOf(true) }
    var showReadiness by rememberSaveable { mutableStateOf(false) }
    var showPlanOptions by rememberSaveable { mutableStateOf(false) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    fun requestBack() {
        if (viewModel.isDraftDirty()) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler { requestBack() }

    LaunchedEffect(uiState.message) {
        val m = uiState.message
        if (m != null && m.contains("Isi minimal")) {
            snackbarHostState.showSnackbar(m)
            viewModel.clearMessage()
        }
    }
    var prevReady by remember { mutableStateOf(uiState.readyCountdownSec) }
    var prevRestRunning by remember { mutableStateOf(uiState.restRunning) }
    LaunchedEffect(uiState.readyCountdownSec, soundEnabled) {
        if (soundEnabled && uiState.readyCountdownSec in 1..3 && uiState.readyCountdownSec < prevReady) {
            coachSound.playReadyTick()
        }
        if (uiState.readyCountdownSec in 1..3 && uiState.readyCountdownSec < prevReady) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        prevReady = uiState.readyCountdownSec
    }
    LaunchedEffect(uiState.restRunning, soundEnabled) {
        if (soundEnabled && prevRestRunning && !uiState.restRunning) {
            coachSound.playRestEnd()
        }
        if (prevRestRunning && !uiState.restRunning) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        prevRestRunning = uiState.restRunning
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Isian sesi belum disimpan. Keluar tanpa menyimpan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text("Keluar tanpa simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Lanjut mengisi")
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gate Session") },
                navigationIcon = {
                    TextButton(
                        onClick = { requestBack() },
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = if (immersiveActiveMode) {
            PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        } else {
            PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        },
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            if (!uiState.guidedStarted) {
            Text(
                if (uiState.guidedStarted) "Workout aktif: ikuti instruksi coach per set."
                else "Siapkan rencana, lalu mulai workout.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
            if (!uiState.guidedStarted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                ) {
                    AssetImage(
                        assetPath = "image/illu_gate_enter.png",
                        contentDescription = "Ilustrasi Gate Session",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
            Text(
                "Rencana hari ini: ${uiState.recommendedPlanLabel}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (!uiState.guidedStarted) {
                Button(
                    onClick = {
                        if (soundEnabled) coachSound.playTap()
                        viewModel.startGuidedSession()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .heightIn(min = 50.dp),
                ) { Text("Start Session") }
                Text(
                    "Alur: Ready 3..2..1 -> Set timer -> Rest -> Next latihan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = soundEnabled,
                        onCheckedChange = {
                            viewModel.setCoachSoundEnabled(it)
                            if (it) coachSound.playTap()
                        },
                    )
                    Text("Suara coach aktif", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                val phaseText = when {
                    uiState.readyCountdownSec > 0 -> "Phase: READY"
                    uiState.restRunning -> "Phase: REST"
                    uiState.workoutPaused -> "Phase: PAUSED"
                    uiState.workoutLive -> "Phase: ACTIVE SET"
                    else -> "Phase: TRANSITION"
                }
                AnimatedContent(
                    targetState = phaseText,
                    label = "phase_text_anim",
                ) { phase ->
                    Text(
                        phase,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            if (!uiState.guidedStarted) {
                TextButton(
                    onClick = { showPlanOptions = !showPlanOptions },
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Text(if (showPlanOptions) "Sembunyikan opsi rencana" else "Ubah rencana (opsional)")
                }
            }
            TextButton(
                onClick = {
                    scope.launch {
                        saveButtonRequester.bringIntoView()
                    }
                },
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text("Lompat ke Simpan")
            }
            }
        }

        if (showPlanOptions && !uiState.guidedStarted) item {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Template latihan", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showTemplate = !showTemplate }) {
                    Text(if (showTemplate) "Sembunyikan" else "Tampilkan")
                }
            }
            if (showTemplate) {
                Text(
                    "Satu ketuk mengisi daftar + beban/rep terakhir (jika ada).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(templateScroll)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.applyTemplateDayA() },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Day A")
                    }
                    OutlinedButton(
                        onClick = { viewModel.applyTemplateDayB() },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Day B")
                    }
                    OutlinedButton(
                        onClick = { viewModel.applyTemplateCardio() },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Cardio")
                    }
                }
            }
        }

        if (showPlanOptions && !uiState.guidedStarted) item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tipe sesi", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showType = !showType }) {
                    Text(if (showType) "Sembunyikan" else "Tampilkan")
                }
            }
            if (showType) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(typeScroll)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("A", "B", "Cardio").forEach { type ->
                        OutlinedButton(
                            onClick = { viewModel.setType(type) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(text = if (uiState.type == type) "$type ✓" else type)
                        }
                    }
                }
            }
        }

        if (showAdvanced) item {
            Spacer(Modifier.height(16.dp))
            Text("Opsi lanjutan", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = catalogHg60Only,
                    onClick = { catalogHg60Only = !catalogHg60Only },
                    label = { Text("Saran HG60 saja") },
                )
            }
            Text(
                "Filter menyempitkan menu dropdown nama latihan ke stasiun mesin/kabel HG60.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (uiState.guidedStarted) {
            item {
                val idx = uiState.currentExerciseIndex.coerceIn(0, uiState.sets.lastIndex.coerceAtLeast(0))
                val currentSet = uiState.sets.getOrNull(idx)
                val isCardioStep = currentSet?.let {
                    it.durationSec.isNotBlank() ||
                        it.exerciseName.contains("cardio", ignoreCase = true) ||
                        it.exerciseName.contains("jalan", ignoreCase = true) ||
                        it.exerciseName.contains("jog", ignoreCase = true)
                } ?: false
                CoachPhaseHero(
                    readyCountdownSec = uiState.readyCountdownSec,
                    restSecondsLeft = uiState.restSecondsLeft,
                    restRunning = uiState.restRunning,
                    activeSetElapsedSec = uiState.activeSetElapsedSec,
                    workoutPaused = uiState.workoutPaused,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
                Text(
                    "Latihan ${idx + 1} dari ${uiState.sets.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    if (isCardioStep) "Target coach: cardio Zona 2 selama ${currentSet?.durationSec?.toIntOrNull()?.div(60) ?: 10} menit"
                    else "Target coach: 3 set x 10-12 reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (uiState.readyCountdownSec > 0) {
                    Text(
                        "Ready ${uiState.readyCountdownSec}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                } else if (!isCardioStep) {
                    Text(
                        if (uiState.workoutPaused) "Paused" else "Timer set: ${formatSeconds(uiState.activeSetElapsedSec)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (uiState.workoutPaused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (!isCardioStep) {
                    Text(
                        "Set ${uiState.coachCurrentSet}/${uiState.coachTargetSets}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (uiState.restRunning) {
                        val m = uiState.restSecondsLeft / 60
                        val s = uiState.restSecondsLeft % 60
                        Text(
                            "Rest ${String.format(Locale.US, "%02d:%02d", m, s)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Crossfade(targetState = idx, label = "guided_exercise_step") { currentIdx ->
                    val set = uiState.sets.getOrNull(currentIdx)
                    val cardioStep = set?.let {
                        it.durationSec.isNotBlank() ||
                            it.exerciseName.contains("cardio", ignoreCase = true) ||
                            it.exerciseName.contains("jalan", ignoreCase = true) ||
                            it.exerciseName.contains("jog", ignoreCase = true)
                    } ?: false
                    if (set != null) {
                        ExerciseSetCard(
                            index = currentIdx,
                            set = set,
                            showDuration = cardioStep,
                            catalogHg60Only = catalogHg60Only,
                            sessionViewModel = viewModel,
                            onCatalogFeedback = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            },
                            canRemove = false,
                            onNameChange = { viewModel.updateSet(currentIdx) { old -> old.copy(exerciseName = it) } },
                            onPickCatalog = { picked -> viewModel.applyPickedExercise(currentIdx, picked) },
                            onRepsChange = { viewModel.updateSet(currentIdx) { old -> old.copy(reps = it.filter(Char::isDigit)) } },
                            onWeightChange = { w ->
                                viewModel.updateSet(currentIdx) { old ->
                                    old.copy(weightKg = w.filter { ch -> ch.isDigit() || ch == '.' })
                                }
                            },
                            onDurationChange = { d ->
                                viewModel.updateSet(currentIdx) { old ->
                                    old.copy(durationSec = d.filter(Char::isDigit).take(6))
                                }
                            },
                            onSharpPainChange = { v ->
                                viewModel.updateSet(currentIdx) { old -> old.copy(sharpPain = v) }
                            },
                            showAdvanced = false,
                            onRemove = {},
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (!isCardioStep) {
                        OutlinedButton(
                            onClick = {
                                if (soundEnabled) coachSound.playTap()
                                viewModel.togglePause()
                            },
                            enabled = uiState.readyCountdownSec == 0 && (uiState.workoutLive || uiState.restRunning),
                            modifier = Modifier.weight(1f),
                        ) { Text(if (uiState.workoutPaused) "Resume" else "Pause") }
                    } else {
                        OutlinedButton(
                            onClick = {
                                if (soundEnabled) coachSound.playTap()
                                viewModel.previousExercise()
                            },
                            enabled = idx > 0,
                            modifier = Modifier.weight(1f),
                        ) { Text("Back") }
                    }
                    Spacer(Modifier.size(8.dp))
                    if (uiState.readyCountdownSec > 0) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(1f),
                        ) { Text("Bersiap...") }
                    } else if (isCardioStep) {
                        Button(
                            onClick = {
                                if (soundEnabled) coachSound.playTap()
                                viewModel.nextExercise()
                            },
                            enabled = idx < uiState.sets.lastIndex,
                            modifier = Modifier.weight(1f),
                        ) { Text("Cardio selesai") }
                    } else if (uiState.restRunning) {
                        OutlinedButton(
                            onClick = {
                                if (soundEnabled) coachSound.playTap()
                                viewModel.skipRest()
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("Lewati rest") }
                    } else if (uiState.coachCurrentSet < uiState.coachTargetSets) {
                        Button(
                            onClick = {
                                if (soundEnabled) coachSound.playSetFinish()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.completeCurrentSetAndStartRest()
                            },
                            enabled = uiState.workoutLive && !uiState.workoutPaused && !uiState.restRunning,
                            modifier = Modifier.weight(1f),
                        ) { Text("Finish Set ${uiState.coachCurrentSet}") }
                    } else {
                        Button(
                            onClick = {
                                if (soundEnabled) coachSound.playTap()
                                viewModel.nextExercise()
                            },
                            enabled = idx < uiState.sets.lastIndex,
                            modifier = Modifier.weight(1f),
                        ) { Text("Lanjut latihan") }
                    }
                }
            }
        }

        if (!uiState.guidedStarted) item {
            OutlinedButton(
                onClick = { showAdvanced = !showAdvanced },
                modifier = Modifier
                    .padding(top = 12.dp)
                    .heightIn(min = 48.dp),
            ) { Text(if (showAdvanced) "Sembunyikan opsi lanjutan" else "Tampilkan opsi lanjutan") }
        }

        if (showAdvanced && !uiState.guidedStarted) item {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::setNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Catatan sesi (opsional)") },
                minLines = 2,
            )
        }

        if (showAdvanced && !uiState.guidedStarted) item {
            OutlinedTextField(
                value = uiState.effortRpe,
                onValueChange = viewModel::setEffortRpe,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("RPE sesi 1–10 (opsional)") },
                placeholder = { Text("Perceived exertion") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        if (showAdvanced && !uiState.guidedStarted) item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Readiness (1-5)", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { showReadiness = !showReadiness }) {
                    Text(if (showReadiness) "Sembunyikan" else "Tampilkan")
                }
            }
            if (showReadiness) {
                Text(
                    "Jika tidur/energi <=2 atau pegal >=4, jalankan sesi lebih ringan 20-30%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.sleepQuality,
                        onValueChange = viewModel::setSleepQuality,
                        modifier = Modifier.weight(1f),
                        label = { Text("Tidur") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = uiState.energyLevel,
                        onValueChange = viewModel::setEnergyLevel,
                        modifier = Modifier.weight(1f),
                        label = { Text("Energi") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = uiState.sorenessLevel,
                        onValueChange = viewModel::setSorenessLevel,
                        modifier = Modifier.weight(1f),
                        label = { Text("Pegal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
        }

        if (showAdvanced && !uiState.guidedStarted) item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = uiState.includedRecoveryMobility,
                    onCheckedChange = viewModel::setIncludedRecoveryMobility,
                )
                Text(
                    "Termasuk mobility / cooldown ringan (+${ProgressRules.RECOVERY_MOBILITY_BONUS_XP} XP)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            uiState.message?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (!uiState.guidedStarted && showAdvanced) item {
            if (showAdvanced) {
                Text(
                    "Tip: saran nama menggabungkan katalog default + latihan yang pernah kamu simpan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        if (uiState.guidedStarted) item {
            Button(
                onClick = { viewModel.saveSession(onSaved = onBack) },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .bringIntoViewRequester(saveButtonRequester)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
                    .heightIn(min = 48.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                            .semantics { contentDescription = "Menyimpan sesi" },
                    )
                    Text("Menyimpan...")
                } else {
                    Text("Finish Workout & Simpan")
                }
            }
        }
    }
    }
}

@Composable
private fun ExerciseSetCard(
    index: Int,
    set: DraftExerciseSet,
    showDuration: Boolean,
    catalogHg60Only: Boolean,
    sessionViewModel: SessionViewModel,
    onCatalogFeedback: (String) -> Unit,
    canRemove: Boolean,
    onNameChange: (String) -> Unit,
    onPickCatalog: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onSharpPainChange: (Boolean) -> Unit,
    showAdvanced: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tipsExpanded by remember(set.rowId) { mutableStateOf(false) }
    var showAddCatalogDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    var markCustomAsHg60 by remember(set.rowId) { mutableStateOf(false) }
    var inExerciseCatalog by remember(set.exerciseName) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val entry: CatalogExercise? = remember(set.exerciseName) {
        WorkoutCatalog.entryForName(set.exerciseName)
    }
    val context = LocalContext.current
    val allowCustomCatalog = false
    val localTutorialImagePath = remember(set.exerciseName) {
        WorkoutCatalog.tutorialImageCandidatesForName(set.exerciseName).firstOrNull { candidate ->
            runCatching { context.assets.open(candidate).close(); true }.getOrDefault(false)
        }
    }
    val localTutorialVideoPath = remember(set.exerciseName) {
        WorkoutCatalog.tutorialVideoCandidatesForName(set.exerciseName).firstOrNull { candidate ->
            runCatching { context.assets.open(candidate).close(); true }.getOrDefault(false)
        }
    }

    LaunchedEffect(set.exerciseName) {
        if (set.exerciseName.isBlank()) {
            inExerciseCatalog = false
        } else {
            inExerciseCatalog = sessionViewModel.isNameInExerciseCatalog(set.exerciseName)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Latihan ${index + 1}", style = MaterialTheme.typography.titleMedium)
            if (showAdvanced) {
                ExerciseNameField(
                    value = set.exerciseName,
                    onValueChange = onNameChange,
                    onPickCatalog = onPickCatalog,
                    catalogHg60Only = catalogHg60Only,
                    sessionViewModel = sessionViewModel,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = set.exerciseName.ifBlank { "Latihan belum dipilih" },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (allowCustomCatalog && showAdvanced && entry == null && set.exerciseName.isNotBlank() && !inExerciseCatalog) {
                TextButton(
                    onClick = { showAddCatalogDialog = true },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text("Simpan ke katalog saya")
                }
            }

            localTutorialImagePath?.let { tutorialImage ->
                AssetImage(
                    assetPath = tutorialImage,
                    contentDescription = "Contoh gerakan ${set.exerciseName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentScale = ContentScale.FillWidth,
                )
            }
            localTutorialVideoPath?.let { tutorialVideo ->
                Button(
                    onClick = { showTutorialDialog = true },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .heightIn(min = 44.dp),
                ) {
                    Text("Putar video tutorial offline")
                }
            }
            if (localTutorialImagePath == null && localTutorialVideoPath == null) {
                Text(
                    "Tutorial offline belum tersedia untuk latihan ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            if (localTutorialImagePath == null && localTutorialVideoPath == null) entry?.tutorialVideoUrl?.let { tutorialUrl ->
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(tutorialUrl)))
                        }
                    },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .heightIn(min = 44.dp),
                ) {
                    Text("Lihat video tutorial form")
                }
            }

            if (allowCustomCatalog && showAdvanced && showAddCatalogDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCatalogDialog = false },
                    title = { Text("Tambah ke katalog") },
                    text = {
                        Column {
                            Text(
                                "«${set.exerciseName.trim()}» akan muncul di saran nama (dan di backup DB).",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = markCustomAsHg60,
                                    onCheckedChange = { markCustomAsHg60 = it },
                                )
                                Text(
                                    "Tandai sebagai stasiun HG60 (ikut filter «Saran HG60 saja»)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showAddCatalogDialog = false
                                val name = set.exerciseName
                                val hg = markCustomAsHg60
                                scope.launch {
                                    when (
                                        sessionViewModel.addCustomExerciseToCatalog(name, hg)
                                    ) {
                                        AddCustomCatalogResult.Added -> {
                                            inExerciseCatalog = true
                                            onCatalogFeedback("Disimpan ke katalog.")
                                        }
                                        AddCustomCatalogResult.AlreadyExists ->
                                            onCatalogFeedback("Nama ini sudah ada di katalog.")
                                        AddCustomCatalogResult.InvalidName ->
                                            onCatalogFeedback("Nama tidak valid.")
                                    }
                                }
                            },
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCatalogDialog = false }) {
                            Text("Batal")
                        }
                    },
                )
            }
            if (showTutorialDialog) {
                AlertDialog(
                    onDismissRequest = { showTutorialDialog = false },
                    title = { Text("Tutorial ${set.exerciseName}") },
                    text = {
                        val videoPath = localTutorialVideoPath
                        if (videoPath != null) {
                            InAppTutorialViewer(assetPath = videoPath)
                        } else {
                            Text("Video offline tidak tersedia.")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTutorialDialog = false }) { Text("Tutup") }
                    },
                )
            }

            if (showAdvanced) {
                entry?.let { e ->
                Text(
                    text = e.info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    maxLines = if (tipsExpanded) Int.MAX_VALUE else 2,
                    modifier = Modifier.padding(top = 8.dp),
                )
                TextButton(
                    onClick = { tipsExpanded = !tipsExpanded },
                    modifier = Modifier.padding(top = 0.dp),
                ) {
                    Text(if (tipsExpanded) "Sembunyikan tips form" else "Lihat tips form (Lakukan / Hindari)")
                }
                if (tipsExpanded) {
                    CoachingSection(title = "Lakukan", lines = e.dos, accentPrimary = true)
                    CoachingSection(title = "Hindari", lines = e.donts, accentError = true)
                }
            }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = set.reps,
                    onValueChange = onRepsChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = set.weightKg,
                    onValueChange = onWeightChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onWeightChange(shiftWeight(set.weightKg, -2.5f)) },
                    modifier = Modifier.weight(1f),
                ) { Text("-2.5kg") }
                OutlinedButton(
                    onClick = { onWeightChange(shiftWeight(set.weightKg, 0f)) },
                    modifier = Modifier.weight(1f),
                ) { Text("Sama") }
                OutlinedButton(
                    onClick = { onWeightChange(shiftWeight(set.weightKg, 2.5f)) },
                    modifier = Modifier.weight(1f),
                ) { Text("+2.5kg") }
            }
            if (showDuration) {
                if (showAdvanced) {
                    OutlinedTextField(
                        value = set.durationSec,
                        onValueChange = onDurationChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = { Text("Durasi (detik, opsional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { onDurationChange("300") },
                        modifier = Modifier.weight(1f),
                    ) { Text("5m") }
                    OutlinedButton(
                        onClick = { onDurationChange("600") },
                        modifier = Modifier.weight(1f),
                    ) { Text("10m") }
                    OutlinedButton(
                        onClick = { onDurationChange("900") },
                        modifier = Modifier.weight(1f),
                    ) { Text("15m") }
                }
            }
            if (showAdvanced) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = set.sharpPain,
                        onCheckedChange = onSharpPainChange,
                    )
                    Text(
                        "Nyeri tajam di gerakan ini",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (canRemove) {
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Text("Hapus latihan")
                }
            }
        }
    }
}

private fun shiftWeight(raw: String, delta: Float): String {
    val base = raw.toFloatOrNull() ?: 0f
    val target = (base + delta).coerceAtLeast(0f)
    return if (target == target.toInt().toFloat()) target.toInt().toString() else target.toString()
}

@Composable
private fun CoachPhaseHero(
    readyCountdownSec: Int,
    restSecondsLeft: Int,
    restRunning: Boolean,
    activeSetElapsedSec: Int,
    workoutPaused: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress = when {
        readyCountdownSec > 0 -> ((3 - readyCountdownSec).coerceIn(0, 3)) / 3f
        restRunning -> ((90 - restSecondsLeft).coerceIn(0, 90)) / 90f
        else -> (activeSetElapsedSec.coerceIn(0, 60)) / 60f
    }
    val title = when {
        readyCountdownSec > 0 -> "READY"
        restRunning -> "REST"
        workoutPaused -> "PAUSED"
        else -> "ACTIVE"
    }
    val centerText = when {
        readyCountdownSec > 0 -> readyCountdownSec.toString()
        restRunning -> formatSeconds(restSecondsLeft)
        else -> formatSeconds(activeSetElapsedSec)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            androidx.compose.material3.CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(110.dp),
                strokeWidth = 10.dp,
            )
            Text(
                centerText,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/** Hanya path aset bawaan tutorial; cegah injeksi HTML / traversal jika path pernah jadi eksplisit dari data. */
private fun isBundledTutorialMediaAssetPath(path: String): Boolean {
    val p = path.trim().trimStart('/')
    if (p.isEmpty() || p.contains("..")) return false
    return p.startsWith("video/tutorial/") || p.startsWith("image/tutorial/")
}

private fun encodeAssetPathForAndroidAssetUrl(path: String): String =
    path.trim().trimStart('/').split('/').filter { it.isNotEmpty() }.joinToString("/") { Uri.encode(it) }

@Composable
private fun InAppTutorialViewer(assetPath: String) {
    if (!isBundledTutorialMediaAssetPath(assetPath)) {
        Text(
            "Media tutorial tidak valid.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }
    val encodedPath = encodeAssetPathForAndroidAssetUrl(assetPath)
    val lower = assetPath.lowercase()
    val html = if (lower.endsWith(".gif")) {
        """
        <html><head><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
        <body style="margin:0;background:#111;display:flex;align-items:center;justify-content:center;">
          <img src="file:///android_asset/$encodedPath" style="max-width:100%;max-height:100%;" />
        </body></html>
        """.trimIndent()
    } else {
        val mime = when {
            lower.endsWith(".webm") -> "video/webm"
            lower.endsWith(".mp4") -> "video/mp4"
            else -> "video/mp4"
        }
        """
        <html><head><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
        <body style="margin:0;background:#111;">
          <video controls autoplay loop playsinline style="width:100%;height:100%;">
            <source src="file:///android_asset/$encodedPath" type="$mime"/>
          </video>
        </body></html>
        """.trimIndent()
    }
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.allowContentAccess = false
                settings.allowFileAccess = true
            }
        },
        update = { web ->
            web.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
        },
        onRelease = { web ->
            web.stopLoading()
            web.loadUrl("about:blank")
            web.removeAllViews()
            web.destroy()
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
    )
}

private fun formatSeconds(totalSec: Int): String {
    val safe = totalSec.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}

@Composable
private fun CoachingSection(
    title: String,
    lines: List<String>,
    accentPrimary: Boolean = false,
    accentError: Boolean = false,
) {
    if (lines.isEmpty()) return
    val color = when {
        accentError -> MaterialTheme.colorScheme.error
        accentPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = Modifier.padding(top = 10.dp),
    )
    lines.forEach { line ->
        Text(
            text = "• $line",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ExerciseNameField(
    value: String,
    onValueChange: (String) -> Unit,
    onPickCatalog: (String) -> Unit,
    catalogHg60Only: Boolean,
    sessionViewModel: SessionViewModel,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(value, catalogHg60Only) {
        options = sessionViewModel.autocompleteExerciseNames(value, catalogHg60Only)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text("Nama latihan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onPickCatalog(name)
                        expanded = false
                    },
                )
            }
        }
    }
}
