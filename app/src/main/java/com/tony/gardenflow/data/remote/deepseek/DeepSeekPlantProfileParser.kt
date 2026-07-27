package com.tony.gardenflow.data.remote.deepseek

import android.util.Log
import com.tony.gardenflow.BuildConfig
import com.tony.gardenflow.domain.engine.StandardGrowthStages
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.PlantProfile
import com.tony.gardenflow.util.GardenText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class DeepSeekPlantProfileParser @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(raw: String): Result<PlantProfile> = runCatching {
        val cleaned = extractJson(raw)
        val dto = json.decodeFromString<PlantProfileDto>(cleaned)
        normalizeProfile(PlantProfile(
            plantName = dto.plantName,
            variety = dto.variety,
            iconName = normalizeIconName(dto.plantIcon, dto.plantName, dto.variety),
            sourceSummary = dto.sourceSummary,
            wateringIntervalDays = dto.wateringIntervalDays,
            wateringAmountMm = dto.wateringAmountMm,
            fertilisingIntervalDays = dto.fertilisingIntervalDays,
            fertilisingAdvice = dto.fertilisingAdvice,
            rainSkipThresholdMm = dto.rainSkipThresholdMm,
            preferredTempMinC = dto.preferredTempMinC,
            preferredTempMaxC = dto.preferredTempMaxC,
            germinationMinDays = dto.germinationMinDays,
            germinationMaxDays = dto.germinationMaxDays,
            harvestMinDays = dto.harvestMinDays,
            harvestMaxDays = dto.harvestMaxDays,
            notes = parseNotes(dto.notes),
            growthStages = dto.growthStages.map { GrowthStage(it.key, it.label, it.icon, it.startDay, it.endDay) }
        ))
    }.onFailure {
        if (BuildConfig.DEBUG) {
            runCatching { Log.w("DeepSeekParser", "Could not parse AI response: ${raw.take(800)}", it) }
        } else {
            runCatching { Log.w("DeepSeekParser", "Could not parse AI response.") }
        }
    }

    private fun parseNotes(notes: kotlinx.serialization.json.JsonElement?): List<String> = when (notes) {
        null -> emptyList()
        is JsonArray -> notes.mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf(String::isNotBlank) }
        is JsonPrimitive -> listOfNotNull(notes.contentOrNull?.takeIf(String::isNotBlank))
        else -> emptyList()
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("```")) {
            val first = trimmed.indexOf('{')
            val last = trimmed.lastIndexOf('}')
            if (first >= 0 && last > first) return trimmed.substring(first, last + 1)
        }
        val first = trimmed.indexOf('{')
        val last = trimmed.lastIndexOf('}')
        return if (first >= 0 && last > first) trimmed.substring(first, last + 1) else trimmed
    }

    private fun normalizeProfile(profile: PlantProfile): PlantProfile {
        val text = listOf(profile.plantName, profile.variety.orEmpty(), profile.sourceSummary.orEmpty())
            .joinToString(" ")
            .lowercase()
        val harvestRange = normalizedHarvestRange(profile)
        val base = profile.copy(
            harvestMinDays = harvestRange?.first,
            harvestMaxDays = harvestRange?.second,
            growthStages = StandardGrowthStages.normalize(profile.growthStages, profile.plantName)
        )
        return when {
            "lemon" in text || "柠檬" in text ->
                base.copy(
                    iconName = "lemon",
                    wateringIntervalDays = 7,
                    wateringAmountMm = base.wateringAmountMm ?: 20.0,
                    fertilisingIntervalDays = base.fertilisingIntervalDays.coerceAtLeast(60),
                    fertilisingAdvice = base.fertilisingAdvice ?: GardenText.s(
                        "Use a citrus fertiliser during active growth; avoid feeding during cold dormant periods.",
                        "生长期使用柑橘专用肥，寒冷休眠期避免施肥。"
                    ),
                    rainSkipThresholdMm = base.rainSkipThresholdMm.coerceAtLeast(5.0),
                    preferredTempMinC = base.preferredTempMinC ?: 10.0,
                    preferredTempMaxC = base.preferredTempMaxC ?: 35.0,
                    harvestMinDays = base.harvestMinDays ?: 731,
                    harvestMaxDays = base.harvestMaxDays ?: 1095,
                    growthStages = StandardGrowthStages.normalize(base.growthStages, base.plantName)
                )
            else -> base.copy(
                iconName = normalizeIconName(base.iconName, base.plantName, base.variety),
                growthStages = StandardGrowthStages.normalize(base.growthStages, base.plantName)
            )
        }
    }

    private fun normalizeIconName(raw: String?, name: String, variety: String?): String {
        val allowed = setOf(
            "lemon", "tomato", "broccoli", "basil", "carrot", "strawberry", "rose", "sunflower",
            "apple", "orange", "aloe", "cactus", "succulent", "cucumber", "daffodil", "lavender",
            "lettuce", "orchid", "pepper", "tulip", "potato", "plant_other"
        )
        val cleaned = raw.orEmpty()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        if (cleaned in allowed) return cleaned
        val text = "$name ${variety.orEmpty()}".lowercase()
        return when {
            "lemon" in text || "lime" in text || "柠檬" in text || "青柠" in text -> "lemon"
            "tomato" in text || "番茄" in text || "西红柿" in text -> "tomato"
            "broccoli" in text || "西兰花" in text -> "broccoli"
            "basil" in text || "mint" in text || "罗勒" in text || "薄荷" in text -> "basil"
            "bear paw" in text || "cotyledon" in text || "succulent" in text || "多肉" in text -> "succulent"
            "nopalxochia" in text || "disocactus" in text || "orchid cactus" in text || "epiphyllum" in text || "cactus" in text || "仙人掌" in text -> "cactus"
            "carrot" in text || "胡萝卜" in text -> "carrot"
            "strawberry" in text || "草莓" in text -> "strawberry"
            "rose" in text || "玫瑰" in text -> "rose"
            "sunflower" in text || "向日葵" in text -> "sunflower"
            "apple" in text || "苹果" in text -> "apple"
            "orange" in text || "橙" in text -> "orange"
            "aloe" in text || "芦荟" in text -> "aloe"
            "cactus" in text || "仙人掌" in text -> "cactus"
            "succulent" in text || "多肉" in text -> "succulent"
            "cucumber" in text || "黄瓜" in text -> "cucumber"
            "daffodil" in text || "水仙" in text -> "daffodil"
            "lavender" in text || "薰衣草" in text -> "lavender"
            "lettuce" in text || "生菜" in text -> "lettuce"
            "orchid" in text || "兰花" in text -> "orchid"
            "pepper" in text || "chilli" in text || "辣椒" in text -> "pepper"
            "tulip" in text || "郁金香" in text -> "tulip"
            "potato" in text || "土豆" in text || "马铃薯" in text -> "potato"
            else -> "plant_other"
        }
    }

    private fun normalizedHarvestRange(profile: PlantProfile): Pair<Int, Int>? {
        val directMin = profile.harvestMinDays
        val directMax = profile.harvestMaxDays
        if (directMin != null && directMax != null && directMin > 0 && directMax >= directMin) {
            return directMin to directMax
        }
        val stage = profile.growthStages.firstOrNull {
            val key = it.key.lowercase()
            val label = it.label.lowercase()
            ("harvest" in key || "fruit" in key || "harvest" in label || "fruit" in label) &&
                it.startDay > 0 &&
                it.endDay >= it.startDay
        }
        return stage?.let { it.startDay to it.endDay }
    }

    private fun harvestEmoji(name: String): String {
        val n = name.lowercase()
        return when {
            "lemon" in n || "柠檬" in n -> "🍋"
            "tomato" in n || "番茄" in n || "西红柿" in n -> "🍅"
            "broccoli" in n || "西兰花" in n -> "🥦"
            "potato" in n || "土豆" in n || "马铃薯" in n -> "🥔"
            "carrot" in n || "胡萝卜" in n -> "🥕"
            "cucumber" in n || "黄瓜" in n -> "🥒"
            "strawberry" in n || "草莓" in n -> "🍓"
            else -> "🧺"
        }
    }
}
