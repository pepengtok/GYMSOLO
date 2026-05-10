package com.workoutleveling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.ui.home.HomeViewModel
import com.workoutleveling.app.ui.theme.GateCyan

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEnterGate: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenBaseline: () -> Unit,
) {
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val playerSummary by viewModel.playerSummary.collectAsStateWithLifecycle()
    val baselineDone by viewModel.baselineDone.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text("Workout Leveling", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Level ${playerSummary.level} · Rank ${playerSummary.rank} · Streak ${playerSummary.streak} hari · ${playerSummary.xp} XP",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
        )
        Text(
            "Sesi tercatat: $sessionCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 6.dp),
        )
        if (!baselineDone) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 3.dp,
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "Baseline tubuh (opsional)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Sekali isi berat / pinggang membantu lihat tren. Bisa dilewati.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    OutlinedButton(
                        onClick = onOpenBaseline,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                    ) {
                        Text("Isi atau lewati")
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onEnterGate,
            colors = ButtonDefaults.buttonColors(
                containerColor = GateCyan,
                contentColor = Color(0xFF0B0F14),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Enter Gate")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onOpenProgress,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Progress")
        }
        Spacer(Modifier.height(20.dp))

        Text("Riwayat terakhir", style = MaterialTheme.typography.titleLarge)
        if (recentSessions.isEmpty()) {
            Text(
                "Belum ada sesi. Tekan Enter Gate untuk mulai.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            recentSessions.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            item.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        }
    }
}
