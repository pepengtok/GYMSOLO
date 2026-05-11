package com.workoutleveling.app.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.AppDatabase
import java.io.File

object DatabaseExport {
    /** Salin DB ke cache dan kembalikan `content://` URI untuk dibagikan. */
    fun prepareShareUri(context: Context, database: AppDatabase): android.net.Uri? {
        return try {
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
            val src = context.getDatabasePath(WorkoutLevelingApp.DB_FILE_NAME)
            if (!src.exists()) return null
            val out = File(context.cacheDir, "workout_leveling_export_${System.currentTimeMillis()}.db")
            src.copyTo(out, overwrite = true)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                out,
            )
        } catch (_: Exception) {
            null
        }
    }

    fun buildShareIntent(uri: android.net.Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "Workout Leveling database backup")
        }
}
