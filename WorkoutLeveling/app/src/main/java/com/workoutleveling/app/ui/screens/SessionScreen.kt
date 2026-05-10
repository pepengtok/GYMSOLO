@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.domain.catalog.CatalogExercise
import com.workoutleveling.app.domain.catalog.WorkoutCatalog
import com.workoutleveling.app.ui.session.DraftExerciseSet
import com.workoutleveling.app.ui.session.SessionViewModel

@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val templateScroll = rememberScrollState()
    val typeScroll = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gate session") },
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Text(
                "Isi sesi latihan hari ini, lalu simpan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            Text("Template latihan", style = MaterialTheme.typography.titleLarge)
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

        item {
            Spacer(Modifier.height(16.dp))
            Text("Tipe sesi", style = MaterialTheme.typography.titleLarge)
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

        item {
            Spacer(Modifier.height(16.dp))
            Text("Daftar latihan", style = MaterialTheme.typography.titleLarge)
        }

        itemsIndexed(
            items = uiState.sets,
            key = { _, set -> set.rowId },
        ) { index, set ->
            ExerciseSetCard(
                index = index,
                set = set,
                canRemove = uiState.sets.size > 1,
                onNameChange = { viewModel.updateSet(index) { old -> old.copy(exerciseName = it) } },
                onPickCatalog = { picked -> viewModel.applyPickedExercise(index, picked) },
                onRepsChange = { viewModel.updateSet(index) { old -> old.copy(reps = it.filter(Char::isDigit)) } },
                onWeightChange = { w ->
                    viewModel.updateSet(index) { old ->
                        old.copy(weightKg = w.filter { ch -> ch.isDigit() || ch == '.' })
                    }
                },
                onRemove = { viewModel.removeSet(index) },
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        item {
            OutlinedButton(
                onClick = { viewModel.addSet() },
                modifier = Modifier
                    .padding(top = 12.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text("+ Tambah set")
            }
        }

        item {
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

        item {
            Text(
                "Tip: pakai Template atau ketik lalu pilih dari menu saran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        item {
            Button(
                onClick = { viewModel.saveSession(onSaved = onBack) },
                enabled = !uiState.isSaving,
                modifier = Modifier
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
                    Text("Simpan sesi")
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
    canRemove: Boolean,
    onNameChange: (String) -> Unit,
    onPickCatalog: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tipsExpanded by remember(set.rowId) { mutableStateOf(false) }
    val entry: CatalogExercise? = remember(set.exerciseName) {
        WorkoutCatalog.entryForName(set.exerciseName)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Latihan ${index + 1}", style = MaterialTheme.typography.titleMedium)
            ExerciseNameField(
                value = set.exerciseName,
                onValueChange = onNameChange,
                onPickCatalog = onPickCatalog,
                modifier = Modifier.padding(top = 8.dp),
            )

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
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember(value) { WorkoutCatalog.suggestionsForQuery(value) }

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
