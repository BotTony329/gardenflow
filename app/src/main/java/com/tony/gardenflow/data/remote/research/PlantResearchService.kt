package com.tony.gardenflow.data.remote.research

interface PlantResearchService {
    suspend fun researchPlant(query: String): Result<PlantResearch>
}

data class PlantResearch(
    val query: String,
    val title: String?,
    val summary: String?,
    val sourceUrl: String?
)
