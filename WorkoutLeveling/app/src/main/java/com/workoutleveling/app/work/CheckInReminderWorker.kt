package com.workoutleveling.app.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.workoutleveling.app.R
import com.workoutleveling.app.domain.progress.WeeklyQuestTemplates
import java.time.DayOfWeek
import java.time.LocalDate

class CheckInReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        if (today.dayOfWeek != DayOfWeek.MONDAY) {
            return Result.success()
        }
        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val weekKey = WeeklyQuestTemplates.isoWeekPeriodKey(today)
        if (prefs.getString(KEY_LAST_WEEK_NOTIFIED, null) == weekKey) {
            return Result.success()
        }

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Pengingat check-in",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            nm.createNotificationChannel(ch)
        }
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gate_launcher)
            .setContentTitle("Check-in mingguan")
            .setContentText("Luangkan waktu untuk berat / lingkar pinggang (opsional).")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(NOTIF_ID, notif)
        prefs.edit().putString(KEY_LAST_WEEK_NOTIFIED, weekKey).apply()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "check_in_monday_reminder"
        private const val CHANNEL_ID = "check_in_reminder"
        private const val NOTIF_ID = 4001
        private const val PREFS = "check_in_worker"
        private const val KEY_LAST_WEEK_NOTIFIED = "last_week_notified"
    }
}
