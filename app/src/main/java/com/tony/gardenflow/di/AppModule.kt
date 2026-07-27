package com.tony.gardenflow.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tony.gardenflow.data.local.GardenDatabase
import com.tony.gardenflow.data.remote.deepseek.AiPlantService
import com.tony.gardenflow.data.remote.deepseek.DeepSeekService
import com.tony.gardenflow.data.remote.weather.GeocodingService
import com.tony.gardenflow.data.remote.weather.OpenMeteoGeocodingService
import com.tony.gardenflow.data.remote.weather.OpenMeteoWeatherService
import com.tony.gardenflow.data.remote.weather.WeatherService
import com.tony.gardenflow.data.repository.DefaultGardenRepository
import com.tony.gardenflow.domain.engine.DefaultReminderEngine
import com.tony.gardenflow.domain.engine.ReminderEngine
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.ocr.MlKitOcrService
import com.tony.gardenflow.ocr.OcrService
import com.tony.gardenflow.security.AppIntegrityService
import com.tony.gardenflow.security.DefaultAppIntegrityService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {
    @Binds abstract fun bindRepository(impl: DefaultGardenRepository): GardenRepository
    @Binds abstract fun bindReminderEngine(impl: DefaultReminderEngine): ReminderEngine
    @Binds abstract fun bindAiPlantService(impl: DeepSeekService): AiPlantService
    @Binds abstract fun bindWeatherService(impl: OpenMeteoWeatherService): WeatherService
    @Binds abstract fun bindGeocodingService(impl: OpenMeteoGeocodingService): GeocodingService
    @Binds abstract fun bindOcrService(impl: MlKitOcrService): OcrService
    @Binds abstract fun bindAppIntegrityService(impl: DefaultAppIntegrityService): AppIntegrityService
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GardenDatabase =
        Room.databaseBuilder(context, GardenDatabase::class.java, "gardenflow.db")
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .build()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE plants ADD COLUMN iconName TEXT")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE plants ADD COLUMN wateringAmountMm REAL")
            db.execSQL("ALTER TABLE plants ADD COLUMN fertilisingAdvice TEXT")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE plants ADD COLUMN photoUri TEXT")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE app_settings ADD COLUMN languageCode TEXT")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS plant_photos (
                    id TEXT NOT NULL,
                    plantId TEXT NOT NULL,
                    uri TEXT NOT NULL,
                    capturedAt TEXT NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_growth_stages_plantId ON growth_stages(plantId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_garden_tasks_plantId ON garden_tasks(plantId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_garden_tasks_status ON garden_tasks(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_garden_tasks_dueAt ON garden_tasks(dueAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_care_history_plantId ON care_history(plantId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_care_history_performedAt ON care_history(performedAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_photos_plantId ON plant_photos(plantId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_photos_capturedAt ON plant_photos(capturedAt)")
        }
    }

    @Provides fun providePlantDao(db: GardenDatabase) = db.plantDao()
    @Provides fun provideStageDao(db: GardenDatabase) = db.growthStageDao()
    @Provides fun provideTaskDao(db: GardenDatabase) = db.gardenTaskDao()
    @Provides fun provideHistoryDao(db: GardenDatabase) = db.careHistoryDao()
    @Provides fun providePlantPhotoDao(db: GardenDatabase) = db.plantPhotoDao()
    @Provides fun provideSettingsDao(db: GardenDatabase) = db.settingsDao()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
