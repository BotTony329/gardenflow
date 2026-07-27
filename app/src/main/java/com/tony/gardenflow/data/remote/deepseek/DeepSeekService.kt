package com.tony.gardenflow.data.remote.deepseek

import com.tony.gardenflow.BuildConfig
import com.tony.gardenflow.domain.model.PlantProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekService @Inject constructor(
    private val client: OkHttpClient,
    private val parser: DeepSeekPlantProfileParser
) : AiPlantService {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generatePlantProfile(
        plantName: String?,
        packageText: String?,
        sowingDate: LocalDate
    ): Result<PlantProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val url = BuildConfig.DEEPSEEK_API_URL.trim()
            val key = BuildConfig.DEEPSEEK_API_KEY.trim()
            val model = BuildConfig.DEEPSEEK_MODEL.trim()
            require(url.isNotBlank()) { "DeepSeek API URL is not configured." }
            require(key.isNotBlank()) { "DeepSeek API key is not configured." }
            require(model.isNotBlank()) { "DeepSeek model is not configured." }

            val body = json.encodeToString(
                DeepSeekRequest(
                    model = model,
                    messages = listOf(
                        DeepSeekMessage("system", systemPrompt()),
                        DeepSeekMessage("user", userPrompt(plantName, packageText, sowingDate))
                    )
                )
            )
            val text = executeDeepSeekRequest(url, key, body)
            val content = runCatching { json.decodeFromString<DeepSeekResponse>(text).choices.firstOrNull()?.message?.content }.getOrNull()
                ?: text
            parser.parse(content).getOrThrow()
        }
    }

    private fun executeDeepSeekRequest(url: String, key: String, body: String): String {
        var lastNetworkError: IOException? = null
        repeat(2) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) error("DeepSeek request failed: HTTP ${response.code}")
                    return text
                }
            } catch (e: IOException) {
                lastNetworkError = e
            }
        }
        throw lastNetworkError ?: IOException("DeepSeek network request failed.")
    }

    private fun systemPrompt() = """
        You are GardenFlow's resident botanist and practical horticulture advisor.
        Your job is to guide a home gardener with professional, conservative, location-aware planting care.
        User input may be English or Chinese. Understand either language, but return JSON string values in English for stable app parsing and local translation.
        Think like a plant scientist first: identify whether the input is a vegetable, herb, ornamental, fruiting shrub, tree, seedling, bulb, or succulent before choosing schedules.
        Then return only the structured JSON needed by the app. Do not chat, do not use Markdown, and do not include explanations outside JSON.
        Prefer stable values for the same plant query. When there are multiple valid care schedules, choose the safer home-garden estimate and explain the assumption briefly in source_summary.
    """.trimIndent()

    private fun userPrompt(plantName: String?, packageText: String?, sowingDate: LocalDate) = """
        user_plant_query=${plantName.orEmpty()}
        planted_date=$sowingDate
        packet_or_user_text=${compactPackageText(packageText)}

        Act as a botanist advising a personal home gardener. Build a practical care plan for the plant entered above.
        Use packet text when present, but correct obvious OCR noise and fill missing values from horticultural knowledge.

        Return exactly one legal JSON object with these GardenFlow fields:
        1. plant_name
        2. variety
        3. plant_icon
        4. watering_interval_days
        5. watering_amount_mm
        6. fertilising_interval_days
        7. fertilising_advice
        8. rain_skip_threshold_mm
        9. preferred_temp_min_c
        10. preferred_temp_max_c
        11. germination_min_days
        12. germination_max_days
        13. harvest_min_days
        14. harvest_max_days
        15. growth_stages with key,label,icon,start_day,end_day
        16. notes, max 3 short care notes
        17. source_summary, one short sentence naming the main assumption, e.g. "Assumes an established dwarf citrus tree in a pot or garden bed."

        Return all human-readable JSON string values in English even when user_plant_query or packet_or_user_text is Chinese. The app will localise display text itself.

        plant_icon must be one of:
        lemon, tomato, broccoli, basil, carrot, strawberry, rose, sunflower, apple, orange,
        aloe, cactus, succulent, cucumber, daffodil, lavender, lettuce, orchid, pepper, tulip, potato, plant_other.

        Expert rules:
        - The packet_or_user_text may contain a line beginning with "User says"; treat that as the gardener's observed current plant state.
        - First classify the plant lifecycle. Do not treat long-lived trees/shrubs as annual vegetables.
        - If the user says the plant is established or mature, create a lifecycle timeline for nursery/established care instead of seed germination, and use established care intervals.
        - Pick the closest plant_icon from the allowed list after identifying the plant species/type. Use plant_other only when none fits.
        - For established fruit trees and shrubs, watering should usually be measured in days between deep watering, not seedling misting.
        - watering_amount_mm is the recommended equivalent depth per watering event for soil/garden beds. Use conservative practical values: seedlings often 5-10 mm, vegetables 10-20 mm, established shrubs/trees 20-35 mm, succulents/cacti lower and less frequent.
        - fertilising_advice must be one concise practical sentence naming fertiliser style and timing.
        - For lemon/citrus trees, use conservative established care unless the text clearly says seed/seedling: watering about 5-7 days, fertilising seasonal or 45-90 days, preferred temperature must be filled.
        - For succulents/cacti, avoid frequent watering; use longer dry intervals and higher rain skip thresholds.
        - For leafy annual vegetables and herbs, use shorter watering intervals and harvest ranges appropriate for annual crops.
        - growth_stages must use only these fixed lifecycle keys and English labels:
          seed = Seed
          germination = Germination
          seedling = Seedling
          establishment = Establishment
          vegetative_growth = Vegetative growth
          flowering = Flowering
          fruiting = Fruiting
          harvest = Harvest
          dormancy = Dormancy
        - Do not invent seasonal or hemisphere-specific stage labels such as "Summer Development". Use the fixed lifecycle labels above and only assign practical start_day/end_day ranges.
        - For seeds/seed packets, include seed/germination/seedling and later stages if relevant.
        - For established or mature plants, skip seed-only stages when inappropriate and start at establishment or vegetative_growth.
        - If planted_date indicates the plant is already old, make growth_stages cover the likely long-lived timeline using the fixed lifecycle keys instead of only germination.
        - If a field is uncertain, return a conservative usable estimate, not 0, null, or placeholder text.
        - Use integer day ranges where possible. Never return harvest_min_days=0 and harvest_max_days=0.
        - notes must be a JSON array of 1-3 short strings, not one combined string.
        - Keep values consistent for the same plant name across regenerations.
        - Return legal JSON only with the requested snake_case keys.
    """.trimIndent()

    private fun compactPackageText(packageText: String?): String {
        val raw = packageText.orEmpty().trim()
        if (raw.isBlank()) return ""
        val useful = raw
            .lineSequence()
            .map { it.trim() }
            .filter { it.length >= 3 }
            .distinct()
            .filter { line ->
                val lower = line.lowercase()
                listOf(
                    "sow", "plant", "water", "harvest", "germinat", "spacing", "sun", "shade", "temperature", "fertilis", "weeks", "days", "cm", "mm",
                    "播种", "种植", "浇水", "施肥", "发芽", "收获", "间距", "阳光", "日照", "遮阴", "温度", "天", "周", "厘米", "毫米"
                )
                    .any { it in lower } || line.any(Char::isDigit)
            }
            .joinToString("; ")
        return useful.ifBlank { raw.replace(Regex("\\s+"), " ") }.take(800)
    }
}
