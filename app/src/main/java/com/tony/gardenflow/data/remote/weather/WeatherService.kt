package com.tony.gardenflow.data.remote.weather

import com.tony.gardenflow.domain.model.WeatherSnapshot

interface WeatherService {
    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherSnapshot>
}
