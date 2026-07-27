package com.tony.gardenflow.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class GardenDeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String
)

@Singleton
class GardenLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Result<Pair<Double, Double>> = runCatching {
        val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
            ?: error("No saved device location is available.")
        location.latitude to location.longitude
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownGardenLocation(): Result<GardenDeviceLocation> = runCatching {
        val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
            ?: error("No saved device location is available.")
        val latitude = location.latitude
        val longitude = location.longitude
        GardenDeviceLocation(
            latitude = latitude,
            longitude = longitude,
            label = reverseGeocode(latitude, longitude) ?: "Lat %.4f, Lon %.4f".format(Locale.US, latitude, longitude)
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val address = Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?: return@runCatching null
            val city = address.locality
                ?: address.subAdminArea
                ?: address.subLocality
                ?: address.adminArea
            val state = address.adminArea
            val country = address.countryName
            listOf(city, state, country)
                .filterNot { it.isNullOrBlank() }
                .distinct()
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
