package com.tony.gardenflow.data.remote.deepseek

import com.tony.gardenflow.domain.model.PlantProfile
import java.time.LocalDate

interface AiPlantService {
    suspend fun generatePlantProfile(
        plantName: String?,
        packageText: String?,
        sowingDate: LocalDate
    ): Result<PlantProfile>
}
