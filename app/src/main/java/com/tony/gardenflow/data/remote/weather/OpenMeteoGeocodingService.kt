package com.tony.gardenflow.data.remote.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenMeteoGeocodingService @Inject constructor(
    private val client: OkHttpClient
) : GeocodingService {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(name: String): Result<GeocodedLocation> = withContext(Dispatchers.IO) {
        runCatching {
            searchCandidatesInternal(name).firstOrNull() ?: error("No matching location found.")
        }
    }

    override suspend fun searchCandidates(name: String): Result<List<GeocodedLocation>> = withContext(Dispatchers.IO) {
        runCatching {
            searchCandidatesInternal(name).ifEmpty { error("No matching location found.") }
        }
    }

    private fun searchCandidatesInternal(name: String): List<GeocodedLocation> {
            val url = "https://geocoding-api.open-meteo.com/v1/search".toHttpUrl().newBuilder()
                .addQueryParameter("name", name)
                .addQueryParameter("count", "10")
                .addQueryParameter("language", "en")
                .addQueryParameter("format", "json")
                .build()
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Location search failed: HTTP ${response.code}")
            val candidates = json.parseToJsonElement(text).jsonObject["results"]?.jsonArray.orEmpty().map { it.jsonObject }
            return candidates
                .sortedBy { if (it["country_code"]?.jsonPrimitive?.content == "AU") 0 else 1 }
                .mapNotNull { result ->
                    val place = listOfNotNull(
                        result["name"]?.jsonPrimitive?.content,
                        result["admin1"]?.jsonPrimitive?.content,
                        result["country"]?.jsonPrimitive?.content
                    ).distinct().joinToString(", ")
                    val latitude = result["latitude"]?.jsonPrimitive?.double
                    val longitude = result["longitude"]?.jsonPrimitive?.double
                    if (place.isBlank() || latitude == null || longitude == null) null else {
                        GeocodedLocation(place, latitude, longitude)
                    }
                }
                .distinctBy { "${it.name}:${"%.4f".format(it.latitude)}:${"%.4f".format(it.longitude)}" }
                .take(6)
    }
}
