package com.tony.gardenflow.data.repository

import com.tony.gardenflow.data.local.entity.AppSettingsEntity
import com.tony.gardenflow.data.local.entity.CareHistoryEntity
import com.tony.gardenflow.data.local.entity.GardenTaskEntity
import com.tony.gardenflow.data.local.entity.GrowthStageEntity
import com.tony.gardenflow.data.local.entity.PlantEntity
import com.tony.gardenflow.data.local.entity.PlantPhotoEntity
import com.tony.gardenflow.domain.model.AppSettings
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.PlantPhoto
import com.tony.gardenflow.domain.model.TaskStatus
import com.tony.gardenflow.domain.model.TaskType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

private val json = Json { ignoreUnknownKeys = true }

fun GrowthStageEntity.toDomain() = GrowthStage(key, label, icon, startDay, endDay)

fun PlantEntity.toDomain(stages: List<GrowthStageEntity>) = Plant(
    id = id,
    name = name,
    variety = variety,
    iconName = iconName,
    sowingDate = sowingDate,
    createdAt = createdAt,
    wateringIntervalDays = wateringIntervalDays,
    wateringAmountMm = wateringAmountMm,
    fertilisingIntervalDays = fertilisingIntervalDays,
    fertilisingAdvice = fertilisingAdvice,
    rainSkipThresholdMm = rainSkipThresholdMm,
    preferredTempMinC = preferredTempMinC,
    preferredTempMaxC = preferredTempMaxC,
    germinationMinDays = germinationMinDays,
    germinationMaxDays = germinationMaxDays,
    harvestMinDays = harvestMinDays,
    harvestMaxDays = harvestMaxDays,
    notes = runCatching { json.parseToJsonElement(notesJson).let { e -> (e as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList() } }.getOrDefault(emptyList()),
    growthStages = stages.map { it.toDomain() },
    packageImageUri = packageImageUri,
    photoUri = photoUri,
    rawPackageText = rawPackageText,
    confirmedStageKey = confirmedStageKey,
    confirmedStageDate = confirmedStageDate
)

fun GardenTaskEntity.toDomain(plantName: String = "") = GardenTask(
    id = id,
    plantId = plantId,
    plantName = plantName,
    type = TaskType.valueOf(type),
    status = TaskStatus.valueOf(status),
    dueAt = dueAt,
    createdAt = createdAt,
    completedAt = completedAt,
    snoozedUntil = snoozedUntil,
    reason = reason
)

fun GardenTask.toEntity() = GardenTaskEntity(id, plantId, type.name, status.name, dueAt, createdAt, completedAt, snoozedUntil, reason)

fun CareHistoryEntity.toDomain() = CareHistory(id, plantId, TaskType.valueOf(actionType), performedAt, note)

fun PlantPhotoEntity.toDomain() = PlantPhoto(id, plantId, uri, capturedAt)

fun AppSettingsEntity.toDomain() = AppSettings(latitude, longitude, locationName, dailyReminderHour, dailyReminderMinute, workFinishHour, workFinishMinute, notificationsEnabled, languageCode)

fun AppSettings.toEntity() = AppSettingsEntity(
    latitude = latitude,
    longitude = longitude,
    locationName = locationName,
    dailyReminderHour = dailyReminderHour,
    dailyReminderMinute = dailyReminderMinute,
    workFinishHour = workFinishHour,
    workFinishMinute = workFinishMinute,
    notificationsEnabled = notificationsEnabled,
    languageCode = languageCode
)

fun notesToJson(notes: List<String>) = json.encodeToString(JsonArray(notes.map { JsonPrimitive(it) }))
