package com.workoutleveling.app.data.local.db

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class DatabaseBackupValidatorTest {
    @Test
    fun hasSqliteHeader_returnsTrueForValidHeader() {
        val header = "SQLite format 3\u0000".toByteArray(StandardCharsets.US_ASCII)
        assertTrue(DatabaseBackupValidator.hasSqliteHeader(header))
    }

    @Test
    fun hasSqliteHeader_returnsFalseForInvalidHeader() {
        val header = "Not SQLite header".toByteArray(StandardCharsets.US_ASCII)
        assertFalse(DatabaseBackupValidator.hasSqliteHeader(header))
    }
}
