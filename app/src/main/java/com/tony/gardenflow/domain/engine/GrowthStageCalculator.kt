package com.tony.gardenflow.domain.engine

import com.tony.gardenflow.domain.model.GrowthStage
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GrowthStageCalculator @Inject constructor() {
    fun calculateCurrentStage(
        sowingDate: LocalDate,
        stages: List<GrowthStage>,
        today: LocalDate
    ): GrowthStage? {
        val day = ChronoUnit.DAYS.between(sowingDate, today).toInt().coerceAtLeast(0)
        return stages.firstOrNull { day in it.startDay..it.endDay }
            ?: stages.maxByOrNull { it.endDay }?.takeIf { day > it.endDay }
    }
}
