package com.tony.gardenflow.domain.model

import java.time.Instant
import java.time.LocalDate

enum class TaskType { WATER, FERTILISE, CHECK_GROWTH, HARVEST, CUSTOM }
enum class TaskStatus { DUE, COMPLETED, SNOOZED, SKIPPED, CANCELLED_BY_WEATHER }

data class GrowthStage(
    val key: String,
    val label: String,
    val icon: String,
    val startDay: Int,
    val endDay: Int
)

data class Plant(
    val id: String,
    val name: String,
    val variety: String?,
    val iconName: String?,
    val sowingDate: LocalDate?,
    val createdAt: Instant,
    val wateringIntervalDays: Int,
    val wateringAmountMm: Double?,
    val fertilisingIntervalDays: Int,
    val fertilisingAdvice: String?,
    val rainSkipThresholdMm: Double,
    val preferredTempMinC: Double?,
    val preferredTempMaxC: Double?,
    val germinationMinDays: Int?,
    val germinationMaxDays: Int?,
    val harvestMinDays: Int?,
    val harvestMaxDays: Int?,
    val notes: List<String>,
    val growthStages: List<GrowthStage>,
    val packageImageUri: String?,
    val photoUri: String?,
    val rawPackageText: String?,
    val confirmedStageKey: String?,
    val confirmedStageDate: LocalDate?
)

data class WeatherSnapshot(
    val currentTemperatureC: Double,
    val rainLast24HoursMm: Double,
    val rainNext12HoursMm: Double,
    val precipitationProbabilityPercent: Int,
    val maxTemperatureTodayC: Double,
    val minTemperatureTodayC: Double,
    val humidityPercent: Int,
    val fetchedAt: Instant
)

data class GardenTask(
    val id: String,
    val plantId: String,
    val plantName: String,
    val type: TaskType,
    val status: TaskStatus,
    val dueAt: Instant,
    val createdAt: Instant,
    val completedAt: Instant? = null,
    val snoozedUntil: Instant? = null,
    val reason: String? = null
)

data class CareHistory(
    val id: String,
    val plantId: String,
    val actionType: TaskType,
    val performedAt: Instant,
    val note: String?
)

data class PlantPhoto(
    val id: String,
    val plantId: String,
    val uri: String,
    val capturedAt: Instant
)

data class AppSettings(
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val dailyReminderHour: Int,
    val dailyReminderMinute: Int,
    val workFinishHour: Int,
    val workFinishMinute: Int,
    val notificationsEnabled: Boolean,
    val languageCode: String?
)

data class PlantProfile(
    val plantName: String,
    val variety: String?,
    val iconName: String?,
    val sourceSummary: String?,
    val wateringIntervalDays: Int,
    val wateringAmountMm: Double?,
    val fertilisingIntervalDays: Int,
    val fertilisingAdvice: String?,
    val rainSkipThresholdMm: Double,
    val preferredTempMinC: Double?,
    val preferredTempMaxC: Double?,
    val germinationMinDays: Int?,
    val germinationMaxDays: Int?,
    val harvestMinDays: Int?,
    val harvestMaxDays: Int?,
    val notes: List<String>,
    val growthStages: List<GrowthStage>
)
