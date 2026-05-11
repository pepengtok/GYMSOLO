package com.workoutleveling.app

import android.app.Application
import com.workoutleveling.app.data.local.db.AppDatabase
import com.workoutleveling.app.data.local.db.DatabaseBackupValidator
import com.workoutleveling.app.data.local.db.ExerciseCatalogSeed
import com.workoutleveling.app.data.local.db.DatabaseImportResult
import com.workoutleveling.app.data.local.prefs.UserPreferences
import com.workoutleveling.app.work.CheckInReminderScheduler
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkoutLevelingApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dbLock = Any()

    @Volatile
    private var databaseInstance: AppDatabase? = null

    val database: AppDatabase
        get() = databaseInstance ?: synchronized(dbLock) {
            databaseInstance ?: AppDatabase.getInstance(this).also { databaseInstance = it }
        }

    val userPreferences: UserPreferences by lazy { UserPreferences(this) }

    /**
     * Impor backup: salin ke file sementara, validasi SQLite + schema, lalu ganti file DB.
     * Setelah sukses, panggil [android.app.Activity.recreate] dari UI.
     */
    fun importDatabaseFromStream(input: InputStream): DatabaseImportResult {
        val tmp = File(cacheDir, "db_import_staging.db")
        val safetyBackup = File(cacheDir, "db_import_safety_lastgood.db")
        synchronized(dbLock) {
            val outcome = try {
                tmp.outputStream().use { out -> input.copyTo(out) }
                when (
                    DatabaseBackupValidator.validate(tmp, AppDatabase.SCHEMA_VERSION)
                ) {
                    DatabaseBackupValidator.Result.NotSqlite ->
                        DatabaseImportResult.Failure("File bukan database SQLite yang valid.")
                    DatabaseBackupValidator.Result.VersionTooNew ->
                        DatabaseImportResult.Failure(
                            "Backup dibuat dengan versi aplikasi yang lebih baru. Perbarui Workout Leveling lalu coba lagi.",
                        )
                    DatabaseBackupValidator.Result.NotOurBackup ->
                        DatabaseImportResult.Failure(
                            "File bukan backup Workout Leveling (bukan schema app ini).",
                        )
                    DatabaseBackupValidator.Result.Ok -> {
                        val oldDb = getDatabasePath(DB_FILE_NAME)
                        if (oldDb.exists()) {
                            oldDb.copyTo(safetyBackup, overwrite = true)
                        } else {
                            safetyBackup.delete()
                        }
                        databaseInstance?.close()
                        databaseInstance = null
                        File(oldDb.absolutePath + "-wal").delete()
                        File(oldDb.absolutePath + "-shm").delete()
                        try {
                            tmp.copyTo(oldDb, overwrite = true)
                            databaseInstance = AppDatabase.getInstance(this)
                            DatabaseImportResult.Success
                        } catch (_: Exception) {
                            if (safetyBackup.exists()) {
                                try {
                                    safetyBackup.copyTo(oldDb, overwrite = true)
                                } catch (_: Exception) {
                                }
                            }
                            try {
                                databaseInstance = AppDatabase.getInstance(this)
                            } catch (_: Exception) {
                                databaseInstance = null
                            }
                            DatabaseImportResult.Failure(
                                "Gagal mengimpor (baca atau tulis file gagal).",
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                DatabaseImportResult.Failure("Gagal mengimpor (baca atau tulis file gagal).")
            }
            tmp.delete()
            return outcome
        }
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch(Dispatchers.IO) {
            ExerciseCatalogSeed.ensureSeeded(database.exerciseCatalogDao())
        }
        CheckInReminderScheduler.schedule(this)
    }

    companion object {
        const val DB_FILE_NAME = "workout_leveling.db"
    }
}
