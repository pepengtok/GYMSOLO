package com.workoutleveling.app.data.local.db

sealed interface DatabaseImportResult {
    data object Success : DatabaseImportResult

    data class Failure(val userMessage: String) : DatabaseImportResult
}
