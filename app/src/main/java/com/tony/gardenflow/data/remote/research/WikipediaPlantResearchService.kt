package com.tony.gardenflow.data.remote.research

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WikipediaPlantResearchService @Inject constructor(
    private val client: OkHttpClient
) : PlantResearchService {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun researchPlant(query: String): Result<PlantResearch> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanQuery = query.trim()
            require(cleanQuery.isNotBlank()) { "Enter a plant name first." }
            val searchUrl = "https://en.wikipedia.org/w/rest.php/v1/search/page".toHttpUrl().newBuilder()
                .addQueryParameter("q", cleanQuery)
                .addQueryParameter("limit", "1")
                .build()
            val searchText = client.newCall(request(searchUrl.toString())).execute().use { response ->
                if (!response.isSuccessful) error("Online plant research failed.")
                response.body?.string().orEmpty()
            }
            val page = json.decodeFromString<WikiSearchResponse>(searchText).pages.firstOrNull()
            val title = page?.title ?: cleanQuery
            val summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary".toHttpUrl().newBuilder()
                .addPathSegment(title)
                .build()
            val summaryText = client.newCall(request(summaryUrl.toString())).execute().use { response ->
                if (!response.isSuccessful) return@use ""
                response.body?.string().orEmpty()
            }
            val summary = summaryText.takeIf { it.isNotBlank() }?.let { json.decodeFromString<WikiSummaryResponse>(it) }
            PlantResearch(
                query = cleanQuery,
                title = summary?.title ?: page?.title,
                summary = summary?.extract ?: page?.description,
                sourceUrl = summary?.contentUrls?.desktop?.page
            )
        }
    }

    private fun request(url: String) = Request.Builder()
        .url(url)
        .addHeader("User-Agent", "GardenFlow/0.1")
        .build()
}

@Serializable
private data class WikiSearchResponse(val pages: List<WikiSearchPage> = emptyList())

@Serializable
private data class WikiSearchPage(
    val title: String? = null,
    val description: String? = null
)

@Serializable
private data class WikiSummaryResponse(
    val title: String? = null,
    val extract: String? = null,
    @SerialName("content_urls") val contentUrls: WikiContentUrls? = null
)

@Serializable
private data class WikiContentUrls(val desktop: WikiDesktopUrl? = null)

@Serializable
private data class WikiDesktopUrl(val page: String? = null)
