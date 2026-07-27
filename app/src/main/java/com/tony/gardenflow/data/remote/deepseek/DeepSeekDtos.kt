package com.tony.gardenflow.data.remote.deepseek

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DeepSeekRequest(
    val model: String,
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.0,
    @SerialName("top_p") val topP: Double = 0.1,
    @SerialName("max_tokens") val maxTokens: Int = 1000,
    @SerialName("response_format") val responseFormat: DeepSeekResponseFormat = DeepSeekResponseFormat()
)

@Serializable
data class DeepSeekMessage(val role: String, val content: String)

@Serializable
data class DeepSeekResponseFormat(val type: String = "json_object")

@Serializable
data class DeepSeekResponse(val choices: List<DeepSeekChoice> = emptyList())

@Serializable
data class DeepSeekChoice(val message: DeepSeekMessage? = null)

@Serializable
data class PlantProfileDto(
    @SerialName("plant_name") val plantName: String,
    val variety: String? = null,
    @SerialName("plant_icon") val plantIcon: String? = null,
    @SerialName("source_summary") val sourceSummary: String? = null,
    @SerialName("watering_interval_days") val wateringIntervalDays: Int = 2,
    @SerialName("watering_amount_mm") val wateringAmountMm: Double? = null,
    @SerialName("fertilising_interval_days") val fertilisingIntervalDays: Int = 14,
    @SerialName("fertilising_advice") val fertilisingAdvice: String? = null,
    @SerialName("rain_skip_threshold_mm") val rainSkipThresholdMm: Double = 3.0,
    @SerialName("preferred_temp_min_c") val preferredTempMinC: Double? = null,
    @SerialName("preferred_temp_max_c") val preferredTempMaxC: Double? = null,
    @SerialName("germination_min_days") val germinationMinDays: Int? = null,
    @SerialName("germination_max_days") val germinationMaxDays: Int? = null,
    @SerialName("harvest_min_days") val harvestMinDays: Int? = null,
    @SerialName("harvest_max_days") val harvestMaxDays: Int? = null,
    val notes: JsonElement? = null,
    @SerialName("growth_stages") val growthStages: List<GrowthStageDto> = emptyList()
)

@Serializable
data class GrowthStageDto(
    val key: String,
    val label: String,
    val icon: String,
    @SerialName("start_day") val startDay: Int,
    @SerialName("end_day") val endDay: Int
)
