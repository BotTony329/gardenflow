package com.tony.gardenflow.ui.addplant

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.gardenflow.data.remote.deepseek.AiPlantService
import com.tony.gardenflow.domain.engine.StandardGrowthStages
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.PlantProfile
import com.tony.gardenflow.domain.repository.GardenRepository
import com.tony.gardenflow.ocr.OcrService
import com.tony.gardenflow.util.GardenText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddPlantUiState(
    val plantName: String = "",
    val packageText: String = "",
    val plantStartStatus: PlantStartStatus = PlantStartStatus.NOT_SURE,
    val selectedImageUri: Uri? = null,
    val sowingDate: LocalDate? = LocalDate.now(),
    val lastWateredDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: PlantProfile? = null,
    val saved: Boolean = false
)

enum class PlantStartStatus {
    SEED,
    SEEDLING,
    YOUNG_PLANT,
    ESTABLISHED_PLANT,
    MATURE_PLANT,
    NOT_SURE
}

@HiltViewModel
class AddPlantViewModel @Inject constructor(
    private val ai: AiPlantService,
    private val ocr: OcrService,
    private val repository: GardenRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddPlantUiState())
    val state: StateFlow<AddPlantUiState> = _state

    fun setPlantName(value: String) = _state.update { it.copy(plantName = value) }
    fun setPackageText(value: String) = _state.update { it.copy(packageText = value) }
    fun setPlantStartStatus(value: PlantStartStatus) = _state.update { it.copy(plantStartStatus = value) }
    fun setSowingDate(value: LocalDate?) = _state.update { it.copy(sowingDate = value) }
    fun setLastWateredDate(value: LocalDate?) = _state.update { it.copy(lastWateredDate = value) }
    fun setError(value: String?) = _state.update { it.copy(error = value) }

    fun recognize(uri: Uri) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null, selectedImageUri = uri) }
        val text = ocr.recognizeText(uri)
        _state.update {
            text.fold(
                onSuccess = { s -> it.copy(packageText = s, isLoading = false) },
                onFailure = { e -> it.copy(error = e.message ?: GardenText.s("OCR failed.", "OCR 识别失败。"), isLoading = false) }
            )
        }
    }

    fun generate() = viewModelScope.launch {
        val current = _state.value
        val sowingDate = current.sowingDate ?: LocalDate.now()
        if (current.plantName.isBlank() && current.packageText.isBlank()) {
            _state.update { it.copy(error = GardenText.s("Enter a plant name or scan a seed packet first.", "请先输入植物名称或扫描种子包装。")) }
            return@launch
        }
        _state.update { it.copy(isLoading = true, error = null) }
        val contextText = buildAiContext(current.packageText, current.plantStartStatus)
        val result = ai.generatePlantProfile(current.plantName.ifBlank { null }, contextText.ifBlank { null }, sowingDate)
        _state.update { state ->
            result.fold(
                onSuccess = { p -> state.copy(profile = p, plantName = p.plantName, isLoading = false) },
                onFailure = {
                    val fallback = fallbackProfile(inferPlantName(current.plantName, current.packageText), current.plantStartStatus)
                    state.copy(
                        profile = fallback,
                        plantName = fallback.plantName,
                        isLoading = false,
                        error = GardenText.s(
                            "AI returned incomplete data or the network failed. An offline starter plan is ready; regenerate when the connection is stable.",
                            "AI 返回不完整数据或网络失败。已生成离线初始计划，网络稳定后可重新生成。"
                        )
                    )
                }
            )
        }
    }

    fun save() = viewModelScope.launch {
        val s = _state.value
        val profile = s.profile ?: fallbackProfile(inferPlantName(s.plantName, s.packageText), s.plantStartStatus)
        repository.addPlant(
            profile = profile,
            sowingDate = s.sowingDate,
            lastWateredDate = s.lastWateredDate,
            rawText = s.packageText.ifBlank { null },
            imageUri = s.selectedImageUri?.toString(),
            confirmedStageKey = profile.stageKeyFor(s.plantStartStatus),
            confirmedStageDate = LocalDate.now().takeIf { s.plantStartStatus != PlantStartStatus.NOT_SURE }
        )
        _state.update { it.copy(saved = true) }
    }

    private fun buildAiContext(packageText: String, status: PlantStartStatus): String {
        val statusText = when (status) {
            PlantStartStatus.SEED -> "User says this plant is being grown from seed."
            PlantStartStatus.SEEDLING -> "User says this is a seedling or small starter plant."
            PlantStartStatus.YOUNG_PLANT -> "User says this is a young nursery plant, not a seed."
            PlantStartStatus.ESTABLISHED_PLANT -> "User says this is an established plant already growing."
            PlantStartStatus.MATURE_PLANT -> "User says this is a mature plant; do not build a seed-based timeline."
            PlantStartStatus.NOT_SURE -> "User is not sure whether this is seed, seedling, or established plant."
        }
        return listOf(statusText, packageText.trim()).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    private fun PlantStartStatus.isEstablishedLike(): Boolean =
        this == PlantStartStatus.ESTABLISHED_PLANT || this == PlantStartStatus.MATURE_PLANT

    private fun PlantProfile.stageKeyFor(status: PlantStartStatus): String? {
        if (status == PlantStartStatus.NOT_SURE) return null
        fun firstMatching(vararg words: String): String? = growthStages.firstOrNull { stage ->
            val haystack = "${stage.key} ${stage.label}".lowercase()
            words.any { it in haystack }
        }?.key
        return when (status) {
            PlantStartStatus.SEED -> firstMatching("seed") ?: growthStages.firstOrNull()?.key
            PlantStartStatus.SEEDLING -> firstMatching("seedling", "germination") ?: growthStages.getOrNull(1)?.key
            PlantStartStatus.YOUNG_PLANT -> firstMatching("establishment", "vegetative_growth", "vegetative") ?: growthStages.getOrNull(growthStages.size / 2)?.key
            PlantStartStatus.ESTABLISHED_PLANT -> firstMatching("establishment", "vegetative_growth", "fruiting") ?: growthStages.getOrNull(growthStages.size / 2)?.key
            PlantStartStatus.MATURE_PLANT -> firstMatching("fruiting", "harvest", "flowering", "dormancy") ?: growthStages.lastOrNull()?.key
            PlantStartStatus.NOT_SURE -> null
        }
    }

    private fun inferPlantName(plantName: String, packageText: String): String {
        val manual = plantName.trim()
        if (manual.isNotBlank()) return manual
        val text = packageText.lowercase()
        return when {
            "meyer" in text && "lemon" in text -> "Meyer dwarf lemon"
            "dwarf" in text && "lemon" in text -> "Dwarf lemon"
            "lemon" in text || "柠檬" in text -> "Lemon"
            "tomato" in text || "番茄" in text || "西红柿" in text -> "Tomato"
            "broccoli" in text || "西兰花" in text -> "Broccoli"
            "potato" in text || "土豆" in text || "马铃薯" in text -> "Potato"
            "basil" in text || "罗勒" in text -> "Basil"
            else -> packageText.lineSequence().map { it.trim() }.firstOrNull { it.length in 3..60 } ?: "Garden plant"
        }
    }

    private fun fallbackProfile(name: String, status: PlantStartStatus = PlantStartStatus.NOT_SURE): PlantProfile {
        val lower = name.lowercase()
        val establishedLike = status == PlantStartStatus.ESTABLISHED_PLANT || status == PlantStartStatus.MATURE_PLANT || "lemon" in lower || "柠檬" in lower
        if (establishedLike) {
            return PlantProfile(
                plantName = name,
                variety = if ("dwarf" in lower || "meyer" in lower) "Dwarf lemon" else null,
                iconName = fallbackIconName(name).takeIf { it != "plant_other" } ?: "plant_other",
                sourceSummary = GardenText.s("Offline starter plan", "离线初始计划"),
                wateringIntervalDays = 7,
                wateringAmountMm = 20.0,
                fertilisingIntervalDays = 60,
                fertilisingAdvice = GardenText.s("Use a citrus fertiliser during active growth; reduce feeding in winter.", "生长期使用柑橘专用肥，冬季减少施肥。"),
                rainSkipThresholdMm = 10.0,
                preferredTempMinC = 10.0,
                preferredTempMaxC = 35.0,
                germinationMinDays = null,
                germinationMaxDays = null,
                harvestMinDays = 731,
                harvestMaxDays = 1095,
                notes = listOf("Water deeply, then let the top soil dry slightly", "Treat this as an established plant unless you later confirm a younger stage"),
                growthStages = StandardGrowthStages.fallbackEstablished(name)
            )
        }
        return PlantProfile(
        plantName = name,
        variety = null,
        iconName = fallbackIconName(name),
        sourceSummary = GardenText.s("Manual starter plan", "手动初始计划"),
        wateringIntervalDays = 2,
        wateringAmountMm = 10.0,
        fertilisingIntervalDays = 14,
        fertilisingAdvice = GardenText.s("Use a gentle balanced fertiliser and adjust after observing growth.", "使用温和均衡肥，并根据生长状态调整。"),
        rainSkipThresholdMm = 3.0,
        preferredTempMinC = null,
        preferredTempMaxC = null,
        germinationMinDays = 7,
        germinationMaxDays = 14,
        harvestMinDays = 90,
        harvestMaxDays = 120,
        notes = listOf("Keep soil evenly moist", "Adjust care after observing the plant"),
        growthStages = StandardGrowthStages.fallbackAnnual(name)
    )
    }

    private fun fallbackIconName(name: String): String {
        val lower = name.lowercase()
        return when {
            "lemon" in lower || "lime" in lower || "柠檬" in lower || "青柠" in lower -> "lemon"
            "tomato" in lower || "番茄" in lower || "西红柿" in lower -> "tomato"
            "broccoli" in lower || "西兰花" in lower -> "broccoli"
            "basil" in lower || "mint" in lower || "罗勒" in lower || "薄荷" in lower -> "basil"
            "carrot" in lower || "胡萝卜" in lower -> "carrot"
            "strawberry" in lower || "草莓" in lower -> "strawberry"
            "rose" in lower || "玫瑰" in lower -> "rose"
            "sunflower" in lower || "向日葵" in lower -> "sunflower"
            "apple" in lower || "苹果" in lower -> "apple"
            "orange" in lower || "橙" in lower -> "orange"
            "aloe" in lower || "芦荟" in lower -> "aloe"
            "cactus" in lower || "仙人掌" in lower -> "cactus"
            "succulent" in lower || "多肉" in lower -> "succulent"
            "cucumber" in lower || "黄瓜" in lower -> "cucumber"
            "lavender" in lower || "薰衣草" in lower -> "lavender"
            "lettuce" in lower || "生菜" in lower -> "lettuce"
            "orchid" in lower || "兰花" in lower -> "orchid"
            "pepper" in lower || "chilli" in lower || "辣椒" in lower -> "pepper"
            "tulip" in lower || "郁金香" in lower -> "tulip"
            "potato" in lower || "土豆" in lower || "马铃薯" in lower -> "potato"
            else -> "plant_other"
        }
    }
}
