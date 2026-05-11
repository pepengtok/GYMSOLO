package com.workoutleveling.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.ui.components.AssetImage
import com.workoutleveling.app.ui.home.HomeViewModel
import com.workoutleveling.app.ui.theme.GateCyan

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEnterGate: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenBaseline: () -> Unit,
    onOpenBodyCheckIn: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val playerSummary by viewModel.playerSummary.collectAsStateWithLifecycle()
    val baselineDone by viewModel.baselineDone.collectAsStateWithLifecycle()
    val homeTipsDismissed by viewModel.homeTipsDismissed.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp,
        ) {
            Box {
                AssetImage(
                    assetPath = "image/home_bg_gate.png",
                    contentDescription = "Gate hero",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .graphicsLayer {
                            translationY = -scrollState.value * 0.08f
                        },
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .alpha(0.55f)
                        .graphicsLayer {
                            translationY = -scrollState.value * 0.04f
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x11000000), Color(0xCC0B0F14)),
                            ),
                        ),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 3.dp,
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AssetImage(
                        assetPath = "image/c_launcher_foreground.png",
                        contentDescription = "Logo Workout Leveling",
                        modifier = Modifier.size(34.dp),
                    )
                    Text("Workout Leveling", style = MaterialTheme.typography.headlineSmall)
                }
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusChip("image/icon_xp.png", "${playerSummary.xp} XP")
                    StatusChip("image/icon_rank.png", "Rank ${playerSummary.rank}")
                    StatusChip("image/icon_streak.png", "${playerSummary.streak}d")
                }
                Text(
                    "Level ${playerSummary.level} · Sesi tercatat: $sessionCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        if (!homeTipsDismissed) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Tips cepat", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Gunakan filter \"Saran HG60 saja\" di Gate session, dan centang mobility/cooldown untuk bonus XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    TextButton(
                        onClick = viewModel::dismissHomeTips,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .heightIn(min = 40.dp),
                    ) {
                        Text("Oke, paham")
                    }
                }
            }
        }
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
        Spacer(Modifier.height(16.dp))
        val gateInteraction = remember { MutableInteractionSource() }
        val gatePressed by gateInteraction.collectIsPressedAsState()
        val gateScale by animateFloatAsState(if (gatePressed) 0.98f else 1f, label = "gate_scale")
        Button(
            onClick = onEnterGate,
            interactionSource = gateInteraction,
            colors = ButtonDefaults.buttonColors(
                containerColor = GateCyan,
                contentColor = Color(0xFF0B0F14),
            ),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = gateScale
                    scaleY = gateScale
                }
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
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onOpenBodyCheckIn,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Check-in tubuh")
        }
        Spacer(Modifier.height(20.dp))

        Text("Riwayat terakhir", style = MaterialTheme.typography.titleLarge)
        if (recentSessions.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
            ) {
                AssetImage(
                    assetPath = "image/empty_no_sessions.png",
                    contentDescription = "Belum ada sesi",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                "Belum ada sesi tercatat. Tekan Enter Gate untuk mulai latihan pertama.",
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

@Composable
private fun StatusChip(
    iconAssetPath: String,
    text: String,
) {
    Surface(shape = MaterialTheme.shapes.small, tonalElevation = 1.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AssetImage(
                assetPath = iconAssetPath,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            )
        }
    }
}
