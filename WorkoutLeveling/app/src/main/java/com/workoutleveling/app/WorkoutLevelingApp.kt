package com.workoutleveling.app

import android.app.Application
import com.workoutleveling.app.data.local.db.AppDatabase
import com.workoutleveling.app.data.local.prefs.UserPreferences

class WorkoutLevelingApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
}
