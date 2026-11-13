package com.zhangs_tech.runtomo.activity

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectStepsReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : StepsReader {
    // minSdk 36: Health Connect is part of the platform, so the client always exists.
    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override val permissions = setOf(READ_STEPS, READ_IN_BACKGROUND)

    override suspend fun canReadSteps(): Boolean =
        READ_STEPS in client.permissionController.getGrantedPermissions()

    override suspend fun canReadInBackground(): Boolean {
        val available = client.features.getFeatureStatus(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND,
        ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
        return available && client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    override suspend fun readSteps(start: Instant, end: Instant): Int {
        val result = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        )
        return (result[StepsRecord.COUNT_TOTAL] ?: 0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private companion object {
        val READ_STEPS = HealthPermission.getReadPermission(StepsRecord::class)
        const val READ_IN_BACKGROUND = HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    }
}
