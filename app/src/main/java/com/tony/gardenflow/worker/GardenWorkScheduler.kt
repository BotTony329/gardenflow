package com.tony.gardenflow.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleDaily(hour: Int = 8, minute: Int = 0) {
        val delay = initialDelay(hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyGardenWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("daily_garden_check", ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleWeatherRefresh() {
        val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("weather_refresh", ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleSnooze(taskId: String, delay: Duration) {
        val request = OneTimeWorkRequestBuilder<SnoozeReminderWorker>()
            .setInputData(androidx.work.workDataOf("taskId" to taskId))
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun initialDelay(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }
}
