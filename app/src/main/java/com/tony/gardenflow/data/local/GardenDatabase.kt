package com.tony.gardenflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tony.gardenflow.data.local.converter.Converters
import com.tony.gardenflow.data.local.dao.CareHistoryDao
import com.tony.gardenflow.data.local.dao.GardenTaskDao
import com.tony.gardenflow.data.local.dao.GrowthStageDao
import com.tony.gardenflow.data.local.dao.PlantDao
import com.tony.gardenflow.data.local.dao.PlantPhotoDao
import com.tony.gardenflow.data.local.dao.SettingsDao
import com.tony.gardenflow.data.local.entity.AppSettingsEntity
import com.tony.gardenflow.data.local.entity.CareHistoryEntity
import com.tony.gardenflow.data.local.entity.GardenTaskEntity
import com.tony.gardenflow.data.local.entity.GrowthStageEntity
import com.tony.gardenflow.data.local.entity.PlantEntity
import com.tony.gardenflow.data.local.entity.PlantPhotoEntity

@Database(
    entities = [
        PlantEntity::class,
        GrowthStageEntity::class,
        GardenTaskEntity::class,
        CareHistoryEntity::class,
        AppSettingsEntity::class,
        PlantPhotoEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun growthStageDao(): GrowthStageDao
    abstract fun gardenTaskDao(): GardenTaskDao
    abstract fun careHistoryDao(): CareHistoryDao
    abstract fun plantPhotoDao(): PlantPhotoDao
    abstract fun settingsDao(): SettingsDao
}
