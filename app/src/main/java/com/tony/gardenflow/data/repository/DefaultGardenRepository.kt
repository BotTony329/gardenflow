package com.tony.gardenflow.data.repository

import com.tony.gardenflow.data.local.dao.CareHistoryDao
import com.tony.gardenflow.data.local.dao.GardenTaskDao
import com.tony.gardenflow.data.local.dao.GrowthStageDao
import com.tony.gardenflow.data.local.dao.PlantDao
import com.tony.gardenflow.data.local.dao.PlantPhotoDao
import com.tony.gardenflow.data.local.dao.SettingsDao
import com.tony.gardenflow.data.local.entity.AppSettingsEntity
import com.tony.gardenflow.data.local.entity.CareHistoryEntity
import com.tony.gardenflow.data.local.entity.GrowthStageEntity
import com.tony.gardenflow.data.local.entity.PlantEntity
import com.tony.gardenflow.data.local.entity.PlantPhotoEntity
import com.tony.gardenflow.domain.model.AppSettings
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.PlantProfile
import com.tony.gardenflow.domain.model.TaskStatus
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.domain.repository.GardenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGardenRepository @Inject constructor(
    private val plantDao: PlantDao,
    private val stageDao: GrowthStageDao,
    private val taskDao: GardenTaskDao,
    private val historyDao: CareHistoryDao,
    private val photoDao: PlantPhotoDao,
    private val settingsDao: SettingsDao
) : GardenRepository {
    override fun observePlants(): Flow<List<Plant>> = plantDao.observePlants().map { plants ->
        val stages = stageDao.getAllStages().groupBy { it.plantId }
        plants.map { it.toDomain(stages[it.id].orEmpty()) }
    }

    override fun observeOpenTasks(): Flow<List<GardenTask>> = combine(taskDao.observeOpenTasks(), observePlants()) { tasks, plants ->
        val names = plants.associate { it.id to it.name }
        tasks.map { it.toDomain(names[it.plantId].orEmpty()) }
    }

    override fun observeHistory(): Flow<List<CareHistory>> = historyDao.observeHistory().map { list -> list.map { it.toDomain() } }

    override fun observePlantPhotos(plantId: String) = photoDao.observePhotosForPlant(plantId).map { photos ->
        photos.map { it.toDomain() }
    }

    override fun observeSettings(): Flow<AppSettings> = settingsDao.observeSettings().map { it?.toDomain() ?: defaultSettings() }

    override suspend fun getPlants(): List<Plant> {
        val plants = plantDao.observePlants().first()
        return plants.map { it.toDomain(stageDao.getStagesForPlant(it.id)) }
    }

    override suspend fun getHistory(): List<CareHistory> = historyDao.getHistory().map { it.toDomain() }

    override suspend fun getSettings(): AppSettings = settingsDao.getSettings()?.toDomain() ?: defaultSettings()

    override suspend fun addPlant(
        profile: PlantProfile,
        sowingDate: LocalDate?,
        lastWateredDate: LocalDate?,
        rawText: String?,
        imageUri: String?,
        confirmedStageKey: String?,
        confirmedStageDate: LocalDate?
    ) {
        val id = UUID.randomUUID().toString()
        plantDao.upsertPlant(
            PlantEntity(
                id = id,
                name = profile.plantName,
                variety = profile.variety,
                iconName = profile.iconName,
                sowingDate = sowingDate,
                createdAt = Instant.now(),
                wateringIntervalDays = profile.wateringIntervalDays.coerceAtLeast(1),
                wateringAmountMm = profile.wateringAmountMm,
                fertilisingIntervalDays = profile.fertilisingIntervalDays.coerceAtLeast(1),
                fertilisingAdvice = profile.fertilisingAdvice,
                rainSkipThresholdMm = profile.rainSkipThresholdMm,
                preferredTempMinC = profile.preferredTempMinC,
                preferredTempMaxC = profile.preferredTempMaxC,
                germinationMinDays = profile.germinationMinDays,
                germinationMaxDays = profile.germinationMaxDays,
                harvestMinDays = profile.harvestMinDays,
                harvestMaxDays = profile.harvestMaxDays,
                notesJson = notesToJson(profile.notes),
                packageImageUri = imageUri,
                photoUri = null,
                rawPackageText = rawText,
                confirmedStageKey = confirmedStageKey,
                confirmedStageDate = confirmedStageDate
            )
        )
        stageDao.upsertAll(profile.growthStages.mapIndexed { index, stage ->
            GrowthStageEntity(UUID.randomUUID().toString(), id, stage.key, stage.label, stage.icon, stage.startDay, stage.endDay, index)
        })
        if (lastWateredDate != null) {
            historyDao.insert(
                CareHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    plantId = id,
                    actionType = TaskType.WATER.name,
                    performedAt = lastWateredDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
                    note = "Initial last watered date"
                )
            )
        }
    }

    override suspend fun completeTask(taskId: String) {
        val task = taskDao.getTask(taskId) ?: return
        val now = Instant.now()
        taskDao.update(task.copy(status = TaskStatus.COMPLETED.name, completedAt = now))
        historyDao.insert(CareHistoryEntity(UUID.randomUUID().toString(), task.plantId, task.type, now, "Completed from GardenFlow"))
    }

    override suspend fun recordCareAction(plantId: String, type: TaskType) {
        val now = Instant.now()
        taskDao.completeOpenTasksForPlant(plantId, type.name, now)
        historyDao.insert(
            CareHistoryEntity(
                id = UUID.randomUUID().toString(),
                plantId = plantId,
                actionType = type.name,
                performedAt = now,
                note = "Quick recorded from plant card"
            )
        )
    }

    override suspend fun snoozeTask(taskId: String, until: Instant) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.update(task.copy(status = TaskStatus.SNOOZED.name, snoozedUntil = until, dueAt = until))
    }

    override suspend fun skipTask(taskId: String, reason: String) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.update(task.copy(status = TaskStatus.SKIPPED.name, reason = reason))
        historyDao.insert(CareHistoryEntity(UUID.randomUUID().toString(), task.plantId, task.type, Instant.now(), "Skipped: $reason"))
    }

    override suspend fun replaceDueTasks(tasks: List<GardenTask>) {
        taskDao.clearDueTasks()
        taskDao.upsertAll(tasks.map { it.toEntity() })
    }

    override suspend fun updateSettings(settings: AppSettings) = settingsDao.upsert(settings.toEntity())

    override suspend fun confirmStage(plantId: String, stageKey: String, date: LocalDate) {
        val plant = plantDao.getPlant(plantId) ?: return
        plantDao.upsertPlant(plant.copy(confirmedStageKey = stageKey, confirmedStageDate = date))
    }

    override suspend fun updatePlantPhoto(plantId: String, photoUri: String?) {
        val plant = plantDao.getPlant(plantId) ?: return
        plantDao.upsertPlant(plant.copy(photoUri = photoUri))
    }

    override suspend fun addPlantPhotos(plantId: String, photoUris: List<String>) {
        val savedUris = photoUris.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (savedUris.isEmpty()) return
        val now = Instant.now()
        photoDao.insertAll(
            savedUris.mapIndexed { index, uri ->
                PlantPhotoEntity(
                    id = UUID.randomUUID().toString(),
                    plantId = plantId,
                    uri = uri,
                    capturedAt = now.minusMillis(index.toLong())
                )
            }
        )
        updatePlantPhoto(plantId, savedUris.first())
    }

    override suspend fun deletePlantPhoto(plantId: String, photoId: String, photoUri: String) {
        photoDao.deletePhoto(photoId)
        val plant = plantDao.getPlant(plantId) ?: return
        if (plant.photoUri == photoUri) {
            plantDao.upsertPlant(plant.copy(photoUri = photoDao.getLatestPhotoForPlant(plantId)?.uri))
        }
    }

    override suspend fun deletePlant(plantId: String) {
        photoDao.deleteForPlant(plantId)
        stageDao.deleteForPlant(plantId)
        plantDao.deletePlant(plantId)
    }

    private fun defaultSettings() = AppSettings(null, null, null, 8, 0, 17, 30, true, null)
}
