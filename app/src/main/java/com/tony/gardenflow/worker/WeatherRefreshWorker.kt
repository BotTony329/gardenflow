package com.tony.gardenflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tony.gardenflow.data.remote.weather.WeatherService
import com.tony.gardenflow.domain.repository.GardenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: GardenRepository,
    private val weatherService: WeatherService
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val settings = repository.getSettings()
        if (settings.latitude != null && settings.longitude != null) {
            weatherService.getWeather(settings.latitude, settings.longitude).getOrNull()
        }
        return Result.success()
    }
}
