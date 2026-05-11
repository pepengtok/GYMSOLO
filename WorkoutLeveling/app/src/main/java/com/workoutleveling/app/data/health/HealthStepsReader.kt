package com.workoutleveling.app.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId

object HealthStepsReader {
    fun getClient(context: Context): HealthConnectClient? =
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (_: Exception) {
            null
        }

    fun sdkAvailable(context: Context): Boolean =
        try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Exception) {
            false
        }

    val stepsReadPermission: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    suspend fun readStepsToday(client: HealthConnectClient, zone: ZoneId = ZoneId.systemDefault()): Long {
        val start = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val end = Instant.now()
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end),
        )
        val response = client.readRecords(request)
        return response.records.sumOf { it.count }
    }
}
