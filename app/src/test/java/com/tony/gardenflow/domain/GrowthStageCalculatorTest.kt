package com.tony.gardenflow.domain

import com.tony.gardenflow.domain.engine.GrowthStageCalculator
import com.tony.gardenflow.domain.model.GrowthStage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GrowthStageCalculatorTest {
    private val calculator = GrowthStageCalculator()
    private val sowing = LocalDate.parse("2026-07-01")
    private val stages = listOf(
        GrowthStage("sown", "Sown", "*", 0, 6),
        GrowthStage("germination", "Germination", "*", 7, 14),
        GrowthStage("seedling", "Seedling", "*", 15, 35),
        GrowthStage("harvest", "Harvest", "*", 112, 140)
    )

    @Test fun day0IsSown() = assertEquals("sown", stage(0))
    @Test fun day9IsGermination() = assertEquals("germination", stage(9))
    @Test fun day20IsSeedling() = assertEquals("seedling", stage(20))
    @Test fun day120IsHarvest() = assertEquals("harvest", stage(120))

    private fun stage(day: Long) = calculator.calculateCurrentStage(sowing, stages, sowing.plusDays(day))?.key
}
