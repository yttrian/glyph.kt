package org.yttr.glyph.bot.wrappers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Wikipedia {
    val client = HttpClient {
        defaultRequest {
            url("https://en.wikipedia.org/w/api.php")
            userAgent("glyph-discord-bot (+https://glyph.yttr.org/)")
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class SearchResponse(val query: Query)

    @Serializable
    data class Query(val search: List<SearchResult> = emptyList(), val pages: Map<Long, ExtractResult> = emptyMap())

    @Serializable
    data class SearchResult(val title: String, @SerialName("pageid") val pageId: Long)

    @Serializable
    data class ExtractResult(val title: String, val extract: String)

    /**
     * Searches for a page on Wikipedia
     */
    suspend fun search(query: String, limit: Int = 10): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        return client.get {
            parameter("action", "query")
            parameter("list", "search")
            parameter("srsearch", query)
            parameter("format", "json")
            parameter("srlimit", limit)
        }.body<SearchResponse>().query.search
    }

    /**
     * Get the extract of a page on Wikipedia
     */
    suspend fun extract(pageId: Long): ExtractResult? = client.get {
        parameter("action", "query")
        parameter("format", "json")
        parameter("prop", "extracts")
        parameter("exintro", true)
        parameter("explaintext", true)
        parameter("pageids", pageId)
    }.body<SearchResponse>().query.pages[pageId]
}
