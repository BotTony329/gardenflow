package com.tony.gardenflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tony.gardenflow.data.local.entity.AppSettingsEntity
import com.tony.gardenflow.data.local.entity.CareHistoryEntity
import com.tony.gardenflow.data.local.entity.GardenTaskEntity
import com.tony.gardenflow.data.local.entity.GrowthStageEntity
import com.tony.gardenflow.data.local.entity.PlantEntity
import com.tony.gardenflow.data.local.entity.PlantPhotoEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY createdAt DESC")
    fun observePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlant(id: String): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlant(plant: PlantEntity)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlant(id: String)
}

@Dao
interface GrowthStageDao {
    @Query("SELECT * FROM growth_stages WHERE plantId = :plantId ORDER BY sortOrder")
    suspend fun getStagesForPlant(plantId: String): List<GrowthStageEntity>

    @Query("SELECT * FROM growth_stages ORDER BY sortOrder")
    suspend fun getAllStages(): List<GrowthStageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stages: List<GrowthStageEntity>)

    @Query("DELETE FROM growth_stages WHERE plantId = :plantId")
    suspend fun deleteForPlant(plantId: String)
}

@Dao
interface GardenTaskDao {
    @Query("SELECT * FROM garden_tasks WHERE status IN ('DUE','SNOOZED') ORDER BY dueAt")
    fun observeOpenTasks(): Flow<List<GardenTaskEntity>>

    @Query("SELECT * FROM garden_tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<GardenTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<GardenTaskEntity>)

    @Update
    suspend fun update(task: GardenTaskEntity)

    @Query("SELECT * FROM garden_tasks WHERE id = :id")
    suspend fun getTask(id: String): GardenTaskEntity?

    @Query("UPDATE garden_tasks SET status = 'COMPLETED', completedAt = :completedAt WHERE plantId = :plantId AND type = :type AND status IN ('DUE','SNOOZED')")
    suspend fun completeOpenTasksForPlant(plantId: String, type: String, completedAt: Instant)

    @Query("DELETE FROM garden_tasks WHERE status = 'DUE'")
    suspend fun clearDueTasks()
}

@Dao
interface CareHistoryDao {
    @Query("SELECT * FROM care_history ORDER BY performedAt DESC")
    fun observeHistory(): Flow<List<CareHistoryEntity>>

    @Query("SELECT * FROM care_history ORDER BY performedAt DESC")
    suspend fun getHistory(): List<CareHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CareHistoryEntity)
}

@Dao
interface PlantPhotoDao {
    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt DESC")
    fun observePhotosForPlant(plantId: String): Flow<List<PlantPhotoEntity>>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt DESC LIMIT 1")
    suspend fun getLatestPhotoForPlant(plantId: String): PlantPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PlantPhotoEntity>)

    @Query("DELETE FROM plant_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)

    @Query("DELETE FROM plant_photos WHERE plantId = :plantId")
    suspend fun deleteForPlant(plantId: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)
}
