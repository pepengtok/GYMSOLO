package com.workoutleveling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.workoutleveling.app.ui.WorkoutLevelingAppRoot
import com.workoutleveling.app.ui.theme.GateBackground
import com.workoutleveling.app.ui.theme.WorkoutLevelingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
