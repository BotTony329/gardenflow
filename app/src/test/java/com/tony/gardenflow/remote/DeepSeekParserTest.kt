package com.tony.gardenflow.remote

import com.tony.gardenflow.data.remote.deepseek.DeepSeekPlantProfileParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekParserTest {
    private val parser = DeepSeekPlantProfileParser()

    @Test fun normalJson() {
        val result = parser.parse(sampleJson()).getOrThrow()
        assertEquals("Broccoli Italian Sprouting", result.plantName)
        assertEquals(2, result.wateringIntervalDays)
    }

    @Test fun missingOptionalFields() {
        val result = parser.parse("""{"plant_name":"Basil"}""").getOrThrow()
        assertEquals("Basil", result.plantName)
        assertEquals(14, result.fertilisingIntervalDays)
    }

    @Test fun invalidJsonReturnsFailure() {
        assertTrue(parser.parse("not json").isFailure)
    }

    @Test fun markdownWrappedJsonParses() {
        val result = parser.parse("```json\n${sampleJson()}\n```").getOrThrow()
        assertEquals("Broccoli Italian Sprouting", result.plantName)
    }

    @Test fun notesMayBeString() {
        val result = parser.parse("""{"plant_name":"Lemon","notes":"Water deeply in dry weather."}""").getOrThrow()
        assertEquals(listOf("Water deeply in dry weather."), result.notes)
    }

    @Test fun dwarfLemonTreeCareIsStabilised() {
        val result = parser.parse(
            """
            {
              "plant_name": "Dwarf Lemon Tree",
              "variety": "Meyer Dwarf Lemon",
              "watering_interval_days": 2,
              "fertilising_interval_days": 14,
              "rain_skip_threshold_mm": 3.0,
              "growth_stages": [
                {"key":"fruiting","label":"Fruiting","icon":"*","start_day":731,"end_day":1095}
              ]
            }
            """.trimIndent()
        ).getOrThrow()
        assertEquals(7, result.wateringIntervalDays)
        assertEquals(60, result.fertilisingIntervalDays)
        assertEquals("🍋", result.growthStages.first().icon)
    }

    @Test fun zeroHarvestRangeFallsBackToHarvestStage() {
        val result = parser.parse(
            """
            {
              "plant_name": "Dwarf Lemon Tree",
              "harvest_min_days": 0,
              "harvest_max_days": 0,
              "growth_stages": [
                {"key":"seedling","label":"Seedling","icon":"🌿","start_day":31,"end_day":90},
                {"key":"fruiting","label":"Fruiting","icon":"🍋","start_day":731,"end_day":1095}
              ]
            }
            """.trimIndent()
        ).getOrThrow()
        assertEquals(731, result.harvestMinDays)
        assertEquals(1095, result.harvestMaxDays)
    }

    @Test fun aiStageNamesAreCanonicalised() {
        val result = parser.parse(
            """
            {
              "plant_name": "Dwarf Lemon",
              "growth_stages": [
                {"key":"summer_development","label":"Summer Development","icon":"*","start_day":90,"end_day":180}
              ]
            }
            """.trimIndent()
        ).getOrThrow()
        assertEquals("vegetative_growth", result.growthStages.first().key)
        assertEquals("Vegetative growth", result.growthStages.first().label)
    }

    private fun sampleJson() = """
        {
          "plant_name": "Broccoli Italian Sprouting",
          "variety": "Italian Sprouting",
          "watering_interval_days": 2,
          "fertilising_interval_days": 14,
          "rain_skip_threshold_mm": 3.0,
          "growth_stages": [
            {"key":"sown","label":"Sown","icon":"*","start_day":0,"end_day":6}
          ]
        }
    """.trimIndent()
}
