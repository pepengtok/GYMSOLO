@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.domain.progress.ProgressRules
import com.workoutleveling.app.ui.progress.ProgressViewModel

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
) {
    val player by viewModel.player.collectAsStateWithLifecycle()
    val quests by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val questSummary by viewModel.summaryText.collectAsStateWithLifecycle()

    val levelProgress = (player.xpIntoLevel.toFloat() / ProgressRules.XP_PER_LEVEL).coerceIn(0f, 1f)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                "Quest harian",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 28.dp),
            )
            Text(
                questSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp),
            )
            if (quests.isEmpty()) {
                Text(
                    "Quest akan dibuat otomatis saat buka app. Selesaikan sesi untuk menandai selesai.",
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
                                Text(q.title, style = MaterialTheme.typography.titleMedium)
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
                "Rumus XP mengikuti roadmap: 50 dasar + menit latihan (maks 45) + bonus streak (≥3 hari).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
