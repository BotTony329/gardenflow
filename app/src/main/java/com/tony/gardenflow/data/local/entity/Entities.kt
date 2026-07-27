package com.tony.gardenflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey val id: String,
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
    val notesJson: String,
    val packageImageUri: String?,
    val photoUri: String?,
    val rawPackageText: String?,
    val confirmedStageKey: String?,
    val confirmedStageDate: LocalDate?
)

@Entity(tableName = "growth_stages", indices = [Index("plantId")])
data class GrowthStageEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val key: String,
    val label: String,
    val icon: String,
    val startDay: Int,
    val endDay: Int,
    val sortOrder: Int
)

@Entity(tableName = "garden_tasks", indices = [Index("plantId"), Index("status"), Index("dueAt")])
data class GardenTaskEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val type: String,
    val status: String,
    val dueAt: Instant,
    val createdAt: Instant,
    val completedAt: Instant?,
    val snoozedUntil: Instant?,
    val reason: String?
)

@Entity(tableName = "care_history", indices = [Index("plantId"), Index("performedAt")])
data class CareHistoryEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val actionType: String,
    val performedAt: Instant,
    val note: String?
)

@Entity(tableName = "plant_photos", indices = [Index("plantId"), Index("capturedAt")])
data class PlantPhotoEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val uri: String,
    val capturedAt: Instant
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
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
