package com.workoutleveling.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.workoutleveling.app.ui.WorkoutLevelingAppRoot
import com.workoutleveling.app.ui.theme.GateBackground
import com.workoutleveling.app.ui.theme.WorkoutLevelingTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* izin opsional; worker tetap dijadwalkan */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WorkoutLeveling)
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermissionOnce()
        enableEdgeToEdge()
        setContent {
            WorkoutLevelingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GateBackground,
                ) {
                    WorkoutLevelingAppRoot()
                }
            }
        }
    }

    private fun maybeRequestNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val prefs = getPreferences(MODE_PRIVATE)
        if (prefs.getBoolean(KEY_NOTIF_PROMPT, false)) return
        prefs.edit().putBoolean(KEY_NOTIF_PROMPT, true).apply()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val KEY_NOTIF_PROMPT = "notif_permission_prompt_shown"
    }
}
