package com.tony.gardenflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.notification.GardenNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SnoozeReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: GardenRepository,
    private val notificationManager: GardenNotificationManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.success()
        val task = repository.observeOpenTasks().first().firstOrNull { it.id == taskId } ?: return Result.success()
        notificationManager.notifySingle(task)
        return Result.success()
    }
}
