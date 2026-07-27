package com.tony.gardenflow.data.remote.weather

data class GeocodedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

interface GeocodingService {
    suspend fun search(name: String): Result<GeocodedLocation>
    suspend fun searchCandidates(name: String): Result<List<GeocodedLocation>>
}
