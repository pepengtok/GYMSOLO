package com.workoutleveling.app.data.local.db

import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Validasi file sebelum menimpa database lokal: magic SQLite, [PRAGMA user_version], dan tabel inti app.
 */
object DatabaseBackupValidator {
    private val sqliteMagic = "SQLite format 3\u0000".toByteArray(StandardCharsets.US_ASCII)
    private const val minHeaderBytes = 100
    private const val workoutSessionsTable = "workout_sessions"

    sealed class Result {
        data object Ok : Result()

        /** Bukan file SQLite atau rusak. */
        data object NotSqlite : Result()

        /** Backup dari app versi lebih baru dari schema yang dipahami perangkat ini. */
        data object VersionTooNew : Result()

        /** user_version di luar rentang yang didukung atau tabel inti tidak ada. */
        data object NotOurBackup : Result()
    }

    fun validate(file: File, maxSupportedSchemaVersion: Int): Result {
        if (!file.isFile || file.length() < minHeaderBytes) return Result.NotSqlite
        file.inputStream().use { ins ->
            val head = ByteArray(16)
            if (ins.read(head) != head.size) return Result.NotSqlite
            if (!hasSqliteHeader(head)) return Result.NotSqlite
        }
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
            val userVersion = db.rawQuery("PRAGMA user_version", null).use { c ->
                if (!c.moveToFirst()) return@use 0
                c.getInt(0)
            }
            if (userVersion > maxSupportedSchemaVersion) return Result.VersionTooNew
            if (userVersion < 1) return Result.NotOurBackup
            val hasSessions = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
                arrayOf(workoutSessionsTable),
            ).use { it.moveToFirst() }
            if (!hasSessions) return Result.NotOurBackup
            Result.Ok
        } catch (_: Exception) {
            Result.NotSqlite
        } finally {
            db?.close()
        }
    }

    internal fun hasSqliteHeader(header16Bytes: ByteArray): Boolean =
        header16Bytes.contentEquals(sqliteMagic)
}
