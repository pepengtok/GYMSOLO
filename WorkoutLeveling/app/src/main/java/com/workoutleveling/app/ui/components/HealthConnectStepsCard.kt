package com.workoutleveling.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.workoutleveling.app.data.health.HealthStepsReader
import kotlinx.coroutines.launch

@Composable
fun HealthConnectStepsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember(context) { HealthStepsReader.getClient(context) }
    val sdkOk = remember(context) { HealthStepsReader.sdkAvailable(context) }
    var steps by remember { mutableLongStateOf(-1L) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        permissionDenied = granted.isEmpty()
        if (granted.containsAll(HealthStepsReader.stepsReadPermission)) {
            scope.launch {
                steps = client?.let { HealthStepsReader.readStepsToday(it) } ?: -1L
            }
        }
    }

    LaunchedEffect(client, sdkOk) {
        if (!sdkOk || client == null) return@LaunchedEffect
        val granted = kotlin.runCatching {
            val perms = HealthStepsReader.stepsReadPermission
            perms.all { p -> client.permissionController.getGrantedPermissions().contains(p) }
        }.getOrDefault(false)
        if (granted) {
            steps = HealthStepsReader.readStepsToday(client)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Health Connect (langkah)",
                style = MaterialTheme.typography.titleMedium,
            )
            when {
                !sdkOk -> Text(
                    "Health Connect tidak tersedia di perangkat ini. Untuk Garmin/dll., pasang app Health Connect resmi lalu coba lagi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    modifier = Modifier.padding(top = 8.dp),
                )
                client == null -> Text(
                    "Tidak bisa membuka klien Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                steps >= 0L -> Text(
                    "Perkiraan langkah hari ini: $steps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                permissionDenied -> Text(
                    "Izin baca langkah ditolak.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                else -> Text(
                    "Izinkan baca data langkah untuk ringkasan harian (read-only).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (sdkOk && client != null) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(HealthStepsReader.stepsReadPermission) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text(if (steps >= 0) "Muat ulang langkah" else "Minta izin & baca langkah")
                }
            }
        }
    }
}
