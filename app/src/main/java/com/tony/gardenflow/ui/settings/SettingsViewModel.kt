package com.tony.gardenflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.gardenflow.BuildConfig
import com.tony.gardenflow.data.remote.weather.GeocodedLocation
import com.tony.gardenflow.data.remote.weather.GeocodingService
import com.tony.gardenflow.domain.model.AppSettings
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.location.GardenLocationProvider
import com.tony.gardenflow.util.GardenText
import com.tony.gardenflow.worker.GardenWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GardenRepository,
    private val geocodingService: GeocodingService,
    private val locationProvider: GardenLocationProvider,
    private val scheduler: GardenWorkScheduler
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings(null, null, null, 8, 0, 17, 30, true, null))
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()
    private val _locationCandidates = MutableStateFlow<List<GeocodedLocation>>(emptyList())
    val locationCandidates: StateFlow<List<GeocodedLocation>> = _locationCandidates
    private val _isSearchingLocation = MutableStateFlow(false)
    val isSearchingLocation: StateFlow<Boolean> = _isSearchingLocation

    val aiStatus: String
        get() = if (BuildConfig.DEEPSEEK_API_URL.isBlank() || BuildConfig.DEEPSEEK_API_KEY.isBlank() || BuildConfig.DEEPSEEK_MODEL.isBlank()) {
            "Not working"
        } else "AI configured"

    fun updateLocation(name: String, latitude: Double?, longitude: Double?) = viewModelScope.launch {
        val cleanName = name.trim()
        if (latitude != null && longitude != null) {
            repository.updateSettings(settings.value.copy(locationName = cleanName.ifBlank { null }, latitude = latitude, longitude = longitude))
            _events.emit(GardenText.s("Garden location saved.", "花园位置已保存。"))
            return@launch
        }
        if (cleanName.isBlank()) {
            repository.updateSettings(settings.value.copy(locationName = null, latitude = null, longitude = null))
            _events.emit(GardenText.s("Garden location cleared. Weather is off.", "花园位置已清除，天气规则已关闭。"))
            return@launch
        }
        val found = geocodingService.search(cleanName)
        found.fold(
            onSuccess = {
                repository.updateSettings(settings.value.copy(locationName = it.name, latitude = it.latitude, longitude = it.longitude))
                _events.emit(GardenText.s("Location saved: ${it.name}.", "位置已保存：${it.name}。"))
            },
            onFailure = {
                repository.updateSettings(settings.value.copy(locationName = cleanName, latitude = null, longitude = null))
                _events.emit(GardenText.s("Saved label, but could not find weather coordinates.", "已保存标签，但没找到天气经纬度。"))
            }
        )
    }

    fun clearLocationCandidates() {
        _locationCandidates.value = emptyList()
    }

    fun searchLocationCandidates(name: String) = viewModelScope.launch {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            _events.emit(GardenText.s("Enter a city or suburb first.", "请先输入城市或区域名称。"))
            return@launch
        }
        _isSearchingLocation.value = true
        val found = geocodingService.searchCandidates(cleanName)
        _isSearchingLocation.value = false
        found.fold(
            onSuccess = {
                _locationCandidates.value = it
                _events.emit(GardenText.s("Choose the correct location.", "请选择正确的位置。"))
            },
            onFailure = {
                _locationCandidates.value = emptyList()
                _events.emit(GardenText.s("No matching location found.", "没有找到匹配的位置。"))
            }
        )
    }

    fun selectLocation(location: GeocodedLocation) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(locationName = location.name, latitude = location.latitude, longitude = location.longitude))
        _locationCandidates.value = emptyList()
        _events.emit(GardenText.s("Location saved: ${location.name}.", "位置已保存：${location.name}。"))
    }

    fun usePhoneLocation() = viewModelScope.launch {
        val location = locationProvider.getLastKnownGardenLocation()
        location.fold(
            onSuccess = { deviceLocation ->
                repository.updateSettings(
                    settings.value.copy(
                        locationName = deviceLocation.label,
                        latitude = deviceLocation.latitude,
                        longitude = deviceLocation.longitude
                    )
                )
                _events.emit(GardenText.s("Location saved: ${deviceLocation.label}.", "位置已保存：${deviceLocation.label}。"))
            },
            onFailure = {
                _events.emit(it.message ?: GardenText.s("Could not access phone location.", "无法访问手机定位。"))
            }
        )
    }

    fun updateReminder(hour: Int, minute: Int) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(dailyReminderHour = hour, dailyReminderMinute = minute))
        scheduler.scheduleDaily(hour, minute)
        _events.emit(GardenText.s("Task reminder time saved.", "任务提醒时间已保存。"))
    }

    fun updateNotifications(enabled: Boolean) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(notificationsEnabled = enabled))
        _events.emit(if (enabled) GardenText.s("Notifications enabled.", "通知已开启。") else GardenText.s("Notifications disabled.", "通知已关闭。"))
    }

    fun updateLanguage(languageCode: String?) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(languageCode = languageCode))
        GardenText.setLanguage(languageCode)
        _events.emit(GardenText.s("Language updated.", "语言已更新。"))
    }
}
