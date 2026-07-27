package com.tony.gardenflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tony.gardenflow.data.remote.weather.WeatherService
import com.tony.gardenflow.domain.engine.ReminderEngine
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.notification.GardenNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant

@HiltWorker
class DailyGardenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: GardenRepository,
    private val weatherService: WeatherService,
    private val reminderEngine: ReminderEngine,
    private val notificationManager: GardenNotificationManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val settings = repository.getSettings()
            val weather = if (settings.latitude != null && settings.longitude != null) {
                weatherService.getWeather(settings.latitude, settings.longitude).getOrNull()
            } else null
            val tasks = reminderEngine.generateTasks(repository.getPlants(), repository.getHistory(), weather, Instant.now())
            repository.replaceDueTasks(tasks)
            if (settings.notificationsEnabled) notificationManager.notifyDaily(tasks)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
