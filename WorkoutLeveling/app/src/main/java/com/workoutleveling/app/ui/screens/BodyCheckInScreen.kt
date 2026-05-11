@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.ui.bodycheckin.BodyCheckInViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyCheckInScreen(
    viewModel: BodyCheckInViewModel,
    onBack: () -> Unit,
) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val recent by viewModel.recentEntries.collectAsStateWithLifecycle()
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Check-in tubuh") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                "Catat berat / pinggang kapan saja (disarankan ~1× per minggu). Tidak menggantikan baseline di onboarding.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Berat (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = waist,
                onValueChange = { waist = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lingkar pinggang (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan (opsional)") },
                minLines = 2,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.saveCheckIn(weight, waist, notes) {
                        weight = ""
                        waist = ""
                        notes = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Simpan check-in")
            }

            Spacer(Modifier.height(28.dp))
            Text("Riwayat terakhir", style = MaterialTheme.typography.titleLarge)
            if (recent.isEmpty()) {
                Text(
                    "Belum ada entri.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                recent.forEach { row ->
                    val label = if (row.isBaseline) "Baseline" else "Check-in"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "$label · ${dateFmt.format(Date(row.recordedAtEpochMs))}",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            val parts = buildList {
                                row.weightKg?.let { add("$it kg") }
                                row.waistCm?.let { add("$it cm pinggang") }
                            }
                            if (parts.isNotEmpty()) {
                                Text(
                                    parts.joinToString(" · "),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            row.notes?.let { n ->
                                Text(
                                    n,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
