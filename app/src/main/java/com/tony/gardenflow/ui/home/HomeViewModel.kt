package com.tony.gardenflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.gardenflow.data.remote.weather.WeatherService
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.domain.model.WeatherSnapshot
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.util.GardenText
import com.tony.gardenflow.worker.GardenWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import javax.inject.Inject

data class HomeUiState(
    val tasks: List<GardenTask> = emptyList(),
    val plants: List<Plant> = emptyList(),
    val history: List<CareHistory> = emptyList(),
    val greeting: String = "Hello",
    val heroTitle: String = "",
    val weatherLocation: String = "Set garden location",
    val weatherSubtitle: String = "Weather rules inactive",
    val temperatureText: String = "--",
    val weatherIcon: String = "partly_cloudy",
    val weatherAdvice: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GardenRepository,
    private val weatherService: WeatherService,
    private val workScheduler: GardenWorkScheduler
) : ViewModel() {
    private val weather = MutableStateFlow<WeatherSnapshot?>(null)
    private val weatherMessage = MutableStateFlow(GardenText.s("Set garden location for weather", "设置花园位置以启用天气"))
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    private val gardenData = combine(
        repository.observeOpenTasks(),
        repository.observePlants(),
        repository.observeHistory()
    ) { tasks, plants, history ->
        GardenHomeData(tasks, plants, history)
    }

    val state: StateFlow<HomeUiState> = combine(
        gardenData,
        repository.observeSettings(),
        weather,
        weatherMessage
    ) { data, settings, snapshot, message ->
        val location = settings.locationName ?: settings.latitude?.let {
            GardenText.s("Latitude: %.2f, Longitude: %.2f", "纬度：%.2f，经度：%.2f").format(it, settings.longitude)
        } ?: GardenText.s("No garden location", "未设置花园位置")
        HomeUiState(
            tasks = data.tasks,
            plants = data.plants,
            history = data.history,
            greeting = greeting(),
            heroTitle = heroTitle(data.tasks),
            weatherLocation = location,
            weatherSubtitle = snapshot?.summary() ?: message,
            temperatureText = snapshot?.currentTemperatureC?.roundToInt()?.let { "$it°C" } ?: "--",
            weatherIcon = snapshot?.iconKey() ?: "partly_cloudy",
            weatherAdvice = snapshot?.careAdvice() ?: GardenText.s("Weather-aware care is off", "天气照料暂未启用")
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            repository.observeSettings().collectLatest { settings ->
                val lat = settings.latitude
                val lon = settings.longitude
                if (lat == null || lon == null) {
                    weather.value = null
                    weatherMessage.value = GardenText.s("Enter a city or coordinates in Settings", "请在设置中输入城市或经纬度")
                } else {
                    weatherMessage.value = GardenText.s("Refreshing weather...", "正在刷新天气...")
                    val result = weatherService.getWeather(lat, lon)
                    weather.value = result.getOrNull()
                    weatherMessage.value = result.fold(
                        onSuccess = { it.summary() },
                        onFailure = { GardenText.s("Weather unavailable. Tasks use care cycles only.", "天气暂不可用，将只按护理周期提醒。") }
                    )
                }
            }
        }
    }

    fun done(taskId: String) = viewModelScope.launch { repository.completeTask(taskId) }
    fun recordWatering(plantId: String) = recordCareAction(plantId, TaskType.WATER, GardenText.s("Watering recorded", "已记录浇水"))
    fun recordFertilising(plantId: String) = recordCareAction(plantId, TaskType.FERTILISE, GardenText.s("Fertilising recorded", "已记录施肥"))
    fun skip(taskId: String) = viewModelScope.launch { repository.skipTask(taskId, GardenText.s("Skipped today", "今天跳过")) }
    fun snooze(taskId: String) = viewModelScope.launch {
        val until = Instant.now().plus(Duration.ofHours(1))
        repository.snoozeTask(taskId, until)
        workScheduler.scheduleSnooze(taskId, Duration.ofHours(1))
    }

    private fun greeting(): String {
        val hour = LocalTime.now().hour
        return GardenText.greeting(hour)
    }

    private fun heroTitle(tasks: List<GardenTask>): String {
        val count = tasks.map { it.plantId }.distinct().size
        return if (count > 0) {
            GardenText.s(
                "$count plants need your care today",
                "今天有 $count 株植物\n需要你的照料"
            )
        } else {
            GardenText.s(
                "Your garden is all cared for today",
                "今天的花园\n已经照料好了"
            )
        }
    }

    private fun recordCareAction(plantId: String, type: TaskType, message: String) = viewModelScope.launch {
        repository.recordCareAction(plantId, type)
        _events.emit(message)
    }

    private fun WeatherSnapshot.summary(): String = when {
        rainNext12HoursMm >= 3.0 -> GardenText.s("Rain expected: %.1f mm next 12h", "未来 12 小时预计降雨 %.1f mm").format(rainNext12HoursMm)
        rainLast24HoursMm >= 3.0 -> GardenText.s("Recent rain: %.1f mm last 24h", "过去 24 小时降雨 %.1f mm").format(rainLast24HoursMm)
        else -> GardenText.s("No significant rain expected", "预计无明显降雨")
    }

    private fun WeatherSnapshot.iconKey(): String = when {
        rainNext12HoursMm >= 3.0 || rainLast24HoursMm >= 3.0 -> "rainy"
        maxTemperatureTodayC >= 30.0 -> "sunny"
        LocalTime.now().hour !in 6..18 -> "night"
        currentTemperatureC <= 10.0 -> "cloudy"
        else -> "partly_cloudy"
    }

    private fun WeatherSnapshot.careAdvice(): String = when {
        rainNext12HoursMm >= 3.0 -> GardenText.s("Rain may cover watering today", "今天可能由降雨补足浇水")
        rainLast24HoursMm >= 3.0 -> GardenText.s("Soil may still be moist", "土壤可能仍然湿润")
        maxTemperatureTodayC >= 30.0 -> GardenText.s("Check soil earlier in the heat", "高温时请提前检查土壤")
        minTemperatureTodayC <= 3.0 -> GardenText.s("Protect cold-sensitive plants", "注意保护怕冷植物")
        currentTemperatureC <= 10.0 -> GardenText.s("Cool conditions, water gently", "天气偏凉，浇水宜谨慎")
        else -> GardenText.s("Mild, low-rain conditions", "天气平稳，降雨较少")
    }
}

private data class GardenHomeData(
    val tasks: List<GardenTask>,
    val plants: List<Plant>,
    val history: List<CareHistory>
)
