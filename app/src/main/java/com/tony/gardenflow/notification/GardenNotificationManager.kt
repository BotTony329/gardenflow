package com.tony.gardenflow.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tony.gardenflow.MainActivity
import com.tony.gardenflow.R
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.util.GardenText
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL_TASKS, "Garden Tasks", NotificationManager.IMPORTANCE_DEFAULT))
            manager.createNotificationChannel(NotificationChannel(CHANNEL_WEATHER, "Weather Updates", NotificationManager.IMPORTANCE_LOW))
            manager.createNotificationChannel(NotificationChannel(CHANNEL_SNOOZE, "Snoozed Reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    @SuppressLint("MissingPermission")
    fun notifyDaily(tasks: List<GardenTask>) {
        if (tasks.isEmpty()) return
        if (!canPostNotifications()) return
        val summary = tasks.take(4).joinToString("\n") { "${it.plantName} - ${label(it.type)}" }
        val notification = base(CHANNEL_TASKS)
            .setContentTitle(GardenText.s("${tasks.size} garden tasks due", "${tasks.size} 个花园任务到期"))
            .setContentText(tasks.first().plantName)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    @SuppressLint("MissingPermission")
    fun notifySingle(task: GardenTask) {
        if (!canPostNotifications()) return
        val notification = base(CHANNEL_SNOOZE)
            .setContentTitle(GardenText.s("${task.plantName} needs ${label(task.type).lowercase()}", "${task.plantName} 需要${label(task.type)}"))
            .setContentText(task.reason ?: GardenText.s("Open GardenFlow to review this task.", "打开 GardenFlow 查看这个任务。"))
            .build()
        NotificationManagerCompat.from(context).notify(task.id.hashCode(), notification)
    }

    private fun base(channel: String): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun label(type: TaskType) = when (type) {
        TaskType.WATER -> GardenText.water
        TaskType.FERTILISE -> GardenText.fertilise
        TaskType.CHECK_GROWTH -> GardenText.s("Check growth", "检查生长")
        TaskType.HARVEST -> GardenText.s("Harvest", "收获")
        TaskType.CUSTOM -> GardenText.s("Task", "任务")
    }

    companion object {
        const val CHANNEL_TASKS = "garden_tasks"
        const val CHANNEL_WEATHER = "weather_updates"
        const val CHANNEL_SNOOZE = "snoozed_reminders"
    }
}
