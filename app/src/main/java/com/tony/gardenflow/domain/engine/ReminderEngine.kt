package com.tony.gardenflow.domain.engine

import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskStatus
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.domain.model.WeatherSnapshot
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

interface ReminderEngine {
    fun generateTasks(
        plants: List<Plant>,
        history: List<CareHistory>,
        weather: WeatherSnapshot?,
        currentTime: Instant
    ): List<GardenTask>
}

class DefaultReminderEngine @Inject constructor() : ReminderEngine {
    override fun generateTasks(
        plants: List<Plant>,
        history: List<CareHistory>,
        weather: WeatherSnapshot?,
        currentTime: Instant
    ): List<GardenTask> {
        return plants.flatMap { plant ->
            val plantHistory = history.filter { it.plantId == plant.id }
            buildList {
                val waterInterval = adjustedWaterInterval(plant.wateringIntervalDays, weather)
                val daysSinceWater = daysSinceLast(plant, plantHistory, TaskType.WATER, currentTime)
                val wateringDue = daysSinceWater >= waterInterval
                val recentRain = weather?.rainLast24HoursMm?.let { it >= plant.rainSkipThresholdMm } ?: false
                val upcomingRain = weather?.rainNext12HoursMm?.let { it >= plant.rainSkipThresholdMm } ?: false
                if (wateringDue && !recentRain && !upcomingRain) {
                    add(task(plant, TaskType.WATER, currentTime, weatherReason(weather, "No significant rain expected")))
                }

                val daysSinceFertilising = daysSinceLast(plant, plantHistory, TaskType.FERTILISE, currentTime)
                if (daysSinceFertilising >= plant.fertilisingIntervalDays) {
                    add(task(plant, TaskType.FERTILISE, currentTime, "Care interval reached"))
                }
            }
        }
    }

    private fun adjustedWaterInterval(base: Int, weather: WeatherSnapshot?): Int {
        val interval = base.coerceAtLeast(1)
        return if ((weather?.maxTemperatureTodayC ?: 0.0) >= 30.0) (interval - 1).coerceAtLeast(1) else interval
    }

    private fun daysSinceLast(
        plant: Plant,
        history: List<CareHistory>,
        type: TaskType,
        now: Instant
    ): Long {
        val last = history.filter { it.actionType == type }.maxByOrNull { it.performedAt }?.performedAt
        val anchor = last ?: plant.sowingDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant() ?: plant.createdAt
        return ChronoUnit.DAYS.between(anchor, now).coerceAtLeast(0)
    }

    private fun task(plant: Plant, type: TaskType, dueAt: Instant, reason: String?) = GardenTask(
        id = UUID.randomUUID().toString(),
        plantId = plant.id,
        plantName = plant.name,
        type = type,
        status = TaskStatus.DUE,
        dueAt = dueAt,
        createdAt = dueAt,
        reason = reason
    )

    private fun weatherReason(weather: WeatherSnapshot?, ok: String): String =
        if (weather == null) "Weather unavailable" else ok
}
