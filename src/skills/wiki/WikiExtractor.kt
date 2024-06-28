package org.yttr.glyph.skills.wiki

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Handle extracting articles from a specific wiki
 */
abstract class WikiExtractor {
    /**
     * HTTP client for making API requests
     */
    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /**
     * Tries to find an article from a search
     *
     * @param query the search query
     */
    abstract suspend fun getArticle(query: String): WikiArticle?
}
