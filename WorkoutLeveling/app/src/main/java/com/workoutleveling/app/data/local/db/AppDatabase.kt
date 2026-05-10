package com.workoutleveling.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        PlayerStateEntity::class,
        QuestEntity::class,
        BodyMetricsEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun playerStateDao(): PlayerStateDao
    abstract fun questDao(): QuestDao
    abstract fun bodyMetricsDao(): BodyMetricsDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `player_state` (
                        `id` INTEGER NOT NULL,
                        `xpTotal` INTEGER NOT NULL,
                        `streakDays` INTEGER NOT NULL,
                        `lastSessionLocalDate` TEXT,
                        `rankCode` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quests` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `questType` TEXT NOT NULL,
                        `targetValue` INTEGER NOT NULL,
                        `progressValue` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `periodKey` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `body_metrics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `recordedAtEpochMs` INTEGER NOT NULL,
                        `weightKg` REAL,
                        `waistCm` REAL,
                        `notes` TEXT,
                        `isBaseline` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "workout_leveling.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
