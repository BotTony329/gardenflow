package com.tony.gardenflow.domain.repository

import com.tony.gardenflow.domain.model.AppSettings
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.PlantPhoto
import com.tony.gardenflow.domain.model.PlantProfile
import com.tony.gardenflow.domain.model.TaskType
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface GardenRepository {
    fun observePlants(): Flow<List<Plant>>
    fun observeOpenTasks(): Flow<List<GardenTask>>
    fun observeHistory(): Flow<List<CareHistory>>
    fun observePlantPhotos(plantId: String): Flow<List<PlantPhoto>>
    fun observeSettings(): Flow<AppSettings>
    suspend fun getPlants(): List<Plant>
    suspend fun getHistory(): List<CareHistory>
    suspend fun getSettings(): AppSettings
    suspend fun addPlant(
        profile: PlantProfile,
        sowingDate: LocalDate?,
        lastWateredDate: LocalDate?,
        rawText: String?,
        imageUri: String?,
        confirmedStageKey: String? = null,
        confirmedStageDate: LocalDate? = null
    )
    suspend fun completeTask(taskId: String)
    suspend fun recordCareAction(plantId: String, type: TaskType)
    suspend fun snoozeTask(taskId: String, until: Instant)
    suspend fun skipTask(taskId: String, reason: String)
    suspend fun replaceDueTasks(tasks: List<GardenTask>)
    suspend fun updateSettings(settings: AppSettings)
    suspend fun confirmStage(plantId: String, stageKey: String, date: LocalDate)
    suspend fun updatePlantPhoto(plantId: String, photoUri: String?)
    suspend fun addPlantPhotos(plantId: String, photoUris: List<String>)
    suspend fun deletePlantPhoto(plantId: String, photoId: String, photoUri: String)
    suspend fun deletePlant(plantId: String)
}
