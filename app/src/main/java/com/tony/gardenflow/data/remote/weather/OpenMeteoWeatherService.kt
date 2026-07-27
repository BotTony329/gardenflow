package com.tony.gardenflow.data.remote.weather

import com.tony.gardenflow.domain.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenMeteoWeatherService @Inject constructor(
    private val client: OkHttpClient
) : WeatherService {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
                .addQueryParameter("latitude", latitude.toString())
                .addQueryParameter("longitude", longitude.toString())
                .addQueryParameter("current", "temperature_2m,relative_humidity_2m,precipitation,rain")
                .addQueryParameter("hourly", "precipitation,precipitation_probability")
                .addQueryParameter("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                .addQueryParameter("past_days", "1")
                .addQueryParameter("forecast_days", "2")
                .addQueryParameter("timezone", "auto")
                .build()
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Weather request failed: HTTP ${response.code}")
            val root = json.parseToJsonElement(text).jsonObject
            val current = root.objectField("current")
            val hourly = root.objectField("hourly")
            val daily = root.objectField("daily")
            val precipitation = hourly.arrayField("precipitation").map { it.jsonPrimitive.doubleOrNull ?: 0.0 }
            val probability = hourly.arrayField("precipitation_probability").map { it.jsonPrimitive.intOrNull ?: 0 }
            WeatherSnapshot(
                currentTemperatureC = current.doubleField("temperature_2m"),
                rainLast24HoursMm = precipitation.take(24).sum(),
                rainNext12HoursMm = precipitation.drop(24).take(12).sum(),
                precipitationProbabilityPercent = probability.drop(24).take(12).maxOrNull() ?: 0,
                maxTemperatureTodayC = daily.arrayField("temperature_2m_max").firstDouble("temperature_2m_max"),
                minTemperatureTodayC = daily.arrayField("temperature_2m_min").firstDouble("temperature_2m_min"),
                humidityPercent = current.intField("relative_humidity_2m"),
                fetchedAt = Instant.now()
            )
        }
    }

    private fun JsonObject.objectField(name: String): JsonObject =
        this[name]?.jsonObject ?: error("Weather response missing object: $name")

    private fun JsonObject.arrayField(name: String): JsonArray =
        this[name]?.jsonArray ?: error("Weather response missing array: $name")

    private fun JsonObject.doubleField(name: String): Double =
        this[name]?.jsonPrimitive?.doubleOrNull ?: error("Weather response missing number: $name")

    private fun JsonObject.intField(name: String): Int =
        this[name]?.jsonPrimitive?.intOrNull ?: error("Weather response missing integer: $name")

    private fun JsonArray.firstDouble(name: String): Double =
        firstOrNull()?.jsonPrimitive?.doubleOrNull ?: error("Weather response missing first value: $name")
}
