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
        ExerciseCatalogEntity::class,
        PainReportEntity::class,
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun playerStateDao(): PlayerStateDao
    abstract fun questDao(): QuestDao
    abstract fun bodyMetricsDao(): BodyMetricsDao
    abstract fun exerciseCatalogDao(): ExerciseCatalogDao
    abstract fun painReportDao(): PainReportDao

    companion object {
        const val SCHEMA_VERSION = 5

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE workout_sessions ADD COLUMN includedRecoveryMobility INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exercise_catalog` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `equipmentTag` TEXT NOT NULL,
                        `isHg60Station` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_catalog_displayName` ON `exercise_catalog` (`displayName`)",
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pain_reports` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `exerciseName` TEXT NOT NULL,
                        `reportedAtEpochMs` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pain_reports_exerciseName` ON `pain_reports` (`exerciseName`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pain_reports_reportedAtEpochMs` ON `pain_reports` (`reportedAtEpochMs`)",
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                com.workoutleveling.app.WorkoutLevelingApp.DB_FILE_NAME,
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}
