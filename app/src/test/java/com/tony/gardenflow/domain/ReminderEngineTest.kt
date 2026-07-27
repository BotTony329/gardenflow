package com.tony.gardenflow.domain

import com.tony.gardenflow.domain.engine.DefaultReminderEngine
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.domain.model.WeatherSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ReminderEngineTest {
    private val engine = DefaultReminderEngine()
    private val now = Instant.parse("2026-07-23T08:00:00Z")
    private val plant = Plant(
        id = "p1",
        name = "Tomato",
        variety = null,
        iconName = "tomato",
        sowingDate = LocalDate.parse("2026-07-20"),
        createdAt = Instant.parse("2026-07-20T00:00:00Z"),
        wateringIntervalDays = 2,
        wateringAmountMm = 10.0,
        fertilisingIntervalDays = 14,
        fertilisingAdvice = "Use a balanced fertiliser.",
        rainSkipThresholdMm = 3.0,
        preferredTempMinC = null,
        preferredTempMaxC = null,
        germinationMinDays = null,
        germinationMaxDays = null,
        harvestMinDays = null,
        harvestMaxDays = null,
        notes = emptyList(),
        growthStages = listOf(GrowthStage("sown", "Sown", "*", 0, 6)),
        packageImageUri = null,
        photoUri = null,
        rawPackageText = null,
        confirmedStageKey = null,
        confirmedStageDate = null
    )

    @Test fun dueAndNoRainCreatesWateringTask() {
        val tasks = engine.generateTasks(listOf(plant), emptyList(), weather(), now)
        assertTrue(tasks.any { it.type == TaskType.WATER })
    }

    @Test fun recentRainSkipsWateringTask() {
        val tasks = engine.generateTasks(listOf(plant), emptyList(), weather(lastRain = 4.0), now)
        assertFalse(tasks.any { it.type == TaskType.WATER })
    }

    @Test fun upcomingRainSkipsWateringTask() {
        val tasks = engine.generateTasks(listOf(plant), emptyList(), weather(nextRain = 4.0), now)
        assertFalse(tasks.any { it.type == TaskType.WATER })
    }

    @Test fun missingWeatherUsesBaseCycle() {
        val tasks = engine.generateTasks(listOf(plant), emptyList(), null, now)
        assertTrue(tasks.any { it.type == TaskType.WATER && it.reason == "Weather unavailable" })
    }

    @Test fun highTemperatureShortensWateringCycle() {
        val wateredYesterday = listOf(CareHistory("h1", "p1", TaskType.WATER, Instant.parse("2026-07-22T08:00:00Z"), null))
        val tasks = engine.generateTasks(listOf(plant), wateredYesterday, weather(max = 32.0), now)
        assertTrue(tasks.any { it.type == TaskType.WATER })
    }

    @Test fun fertilisingDueCreatesTask() {
        val oldPlant = plant.copy(sowingDate = LocalDate.parse("2026-07-01"), createdAt = Instant.parse("2026-07-01T00:00:00Z"))
        val tasks = engine.generateTasks(listOf(oldPlant), emptyList(), weather(), now)
        assertTrue(tasks.any { it.type == TaskType.FERTILISE })
    }

    private fun weather(lastRain: Double = 0.0, nextRain: Double = 0.0, max: Double = 24.0) = WeatherSnapshot(
        currentTemperatureC = 18.0,
        rainLast24HoursMm = lastRain,
        rainNext12HoursMm = nextRain,
        precipitationProbabilityPercent = 20,
        maxTemperatureTodayC = max,
        minTemperatureTodayC = 10.0,
        humidityPercent = 60,
        fetchedAt = now
    )
}
