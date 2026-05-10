package com.workoutleveling.app.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.workoutleveling.app.ui.baseline.BaselineViewModel
import com.workoutleveling.app.ui.home.HomeViewModel
import com.workoutleveling.app.ui.navigation.Routes
import com.workoutleveling.app.ui.progress.ProgressViewModel
import com.workoutleveling.app.ui.session.SessionViewModel
import com.workoutleveling.app.ui.screens.BaselineScreen
import com.workoutleveling.app.ui.screens.HomeScreen
import com.workoutleveling.app.ui.screens.ProgressScreen
import com.workoutleveling.app.ui.screens.SessionScreen

@Composable
fun WorkoutLevelingAppRoot(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier,
    ) {
        composable(Routes.Home) {
            val app = LocalContext.current.applicationContext as Application
            val vm: HomeViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app),
            )
            HomeScreen(
                viewModel = vm,
                onEnterGate = { navController.navigate(Routes.Session) },
                onOpenProgress = { navController.navigate(Routes.Progress) },
                onOpenBaseline = { navController.navigate(Routes.Baseline) },
            )
        }
        composable(Routes.Session) {
            val app = LocalContext.current.applicationContext as Application
            val vm: SessionViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app),
            )
            SessionScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Progress) {
            val app = LocalContext.current.applicationContext as Application
            val vm: ProgressViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app),
            )
            ProgressScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Baseline) {
            val app = LocalContext.current.applicationContext as Application
            val vm: BaselineViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app),
            )
            BaselineScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() },
            )
        }
    }
}
