package com.tony.gardenflow.util

import com.tony.gardenflow.domain.model.GrowthStage

object GardenPlantText {
    fun stageLabel(stage: GrowthStage): String {
        if (!GardenText.isZh) return stage.label
        val lower = "${stage.key} ${stage.label}".lowercase()
        return when {
            stage.key == "seed" -> "播种期"
            stage.key == "germination" -> "发芽期"
            stage.key == "seedling" -> "幼苗期"
            stage.key == "establishment" -> "定植期"
            stage.key == "vegetative_growth" -> "生长期"
            stage.key == "flowering" -> "开花期"
            stage.key == "fruiting" -> "结果期"
            stage.key == "harvest" -> "收获期"
            stage.key == "dormancy" -> "休眠期"
            "germinat" in lower -> "发芽期"
            "seedling" in lower -> "幼苗期"
            "establish" in lower -> "定植期"
            "summer" in lower && "development" in lower -> "生长期"
            "development" in lower || "vegetative" in lower || "growing" in lower || lower.endsWith(" growth") || lower == "growth" -> "生长期"
            "flower" in lower -> "开花期"
            "fruit" in lower -> "结果期"
            "harvest" in lower -> "收获期"
            "dorman" in lower -> "休眠期"
            "mature" in lower -> "成熟期"
            "sown" in lower || "seed" in lower || "propagation" in lower -> "播种期"
            else -> stage.label
        }
    }

    fun careAdvice(raw: String): String {
        if (!GardenText.isZh) return raw
        val lower = raw.lowercase()
        return when {
            "citrus fertil" in lower || "6-4-6" in lower -> "早春使用均衡柑橘肥，夏季每 6-8 周补施一次；冬季休眠期避免施肥。"
            "balanced fertil" in lower || "gentle" in lower -> "使用温和均衡肥，并根据生长状态调整。"
            raw.hasEnglishOnlyText() -> "根据植物生长期使用合适的温和肥料，并根据状态调整。"
            else -> raw
        }
    }

    private fun String.hasEnglishOnlyText(): Boolean =
        any { it in 'A'..'Z' || it in 'a'..'z' } && none { it in '\u4e00'..'\u9fff' }
}
