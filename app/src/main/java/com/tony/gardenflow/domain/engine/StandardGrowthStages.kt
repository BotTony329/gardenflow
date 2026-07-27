package com.tony.gardenflow.domain.engine

import com.tony.gardenflow.domain.model.GrowthStage

object StandardGrowthStages {
    private data class Template(val key: String, val label: String, val icon: String)

    private val templates = listOf(
        Template("seed", "Seed", "🌰"),
        Template("germination", "Germination", "🌱"),
        Template("seedling", "Seedling", "🌿"),
        Template("establishment", "Establishment", "🌿"),
        Template("vegetative_growth", "Vegetative growth", "🌿"),
        Template("flowering", "Flowering", "🌼"),
        Template("fruiting", "Fruiting", "🍅"),
        Template("harvest", "Harvest", "🧺"),
        Template("dormancy", "Dormancy", "🌙")
    )

    private val templateByKey = templates.associateBy { it.key }

    fun normalize(stages: List<GrowthStage>, plantName: String): List<GrowthStage> {
        val normalized = stages
            .mapNotNull { stage ->
                val key = canonicalKey(stage.key, stage.label) ?: return@mapNotNull null
                val template = templateByKey.getValue(key)
                val start = stage.startDay.coerceAtLeast(0)
                val end = stage.endDay.coerceAtLeast(start)
                GrowthStage(
                    key = key,
                    label = template.label,
                    icon = iconFor(key, plantName),
                    startDay = start,
                    endDay = end
                )
            }
            .groupBy { it.key }
            .map { (_, group) ->
                val first = group.minByOrNull { it.startDay } ?: group.first()
                first.copy(
                    startDay = group.minOf { it.startDay },
                    endDay = group.maxOf { it.endDay }
                )
            }
            .sortedBy { it.startDay }

        return normalized.ifEmpty { fallbackAnnual(plantName) }
    }

    fun fallbackAnnual(plantName: String): List<GrowthStage> = listOf(
        stage("seed", plantName, 0, 6),
        stage("germination", plantName, 7, 14),
        stage("seedling", plantName, 15, 35),
        stage("vegetative_growth", plantName, 36, 89),
        stage("harvest", plantName, 90, 120)
    )

    fun fallbackEstablished(plantName: String): List<GrowthStage> = listOf(
        stage("establishment", plantName, 0, 90),
        stage("vegetative_growth", plantName, 91, 365),
        stage("flowering", plantName, 366, 730),
        stage("fruiting", plantName, 731, 1095)
    )

    fun canonicalKey(key: String, label: String): String? {
        val lower = "$key $label".lowercase()
        return when {
            "germinat" in lower -> "germination"
            "seedling" in lower || "young nursery" in lower || "starter" in lower -> "seedling"
            "establish" in lower || "planted plant" in lower || "nursery plant" in lower -> "establishment"
            "vegetative" in lower || "summer development" in lower || "development" in lower ||
                "growth" in lower || "growing" in lower || "mature growth" in lower -> "vegetative_growth"
            "flower" in lower || "bloom" in lower -> "flowering"
            "fruit" in lower -> "fruiting"
            "harvest" in lower || "crop" in lower -> "harvest"
            "dorman" in lower || "winter" in lower -> "dormancy"
            "sown" in lower || "seed packet" in lower || lower.contains(" seed") || lower.startsWith("seed ") -> "seed"
            else -> key.lowercase().takeIf { it in templateByKey }
        }
    }

    private fun stage(key: String, plantName: String, start: Int, end: Int): GrowthStage {
        val template = templateByKey.getValue(key)
        return GrowthStage(key, template.label, iconFor(key, plantName), start, end)
    }

    private fun iconFor(key: String, plantName: String): String {
        if (key == "harvest" || key == "fruiting") return harvestIcon(plantName)
        return templateByKey.getValue(key).icon
    }

    private fun harvestIcon(name: String): String {
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
