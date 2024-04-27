package org.yttr.glyph.bot.skills.wiki

import io.ktor.client.features.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPath
import io.ktor.http.takeFrom
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Grabs articles from Wikipedia
 */
class WikipediaExtractor(
    /**
     * Specifies what edition of Wikipedia to use
     */
    private val languageCode: String = "en"
) : WikiExtractor() {

    companion object {
        /**
         * Page ID used when a query fails
         */
        const val INVALID_PAGE_ID: Int = -1

        /**
         * Represents a thumbnail result
         */
        @Serializable
        data class Thumbnail(
            /**
             * URL of the thumbnail image
             */
            val source: String
        )

        /**
         * Represents a page on Wikipedia
         */
        @Serializable
        data class Page(
            /**
             * Title of the page
             */
            val title: String,
            /**
             * Excerpt from the page
             */
            val extract: String,
            /**
             * URL linking to the page
             */
            @SerialName("fullurl")
            val fullUrl: String,
            /**
             * Thumbnail, if any
             */
            val thumbnail: Thumbnail?
        )

        /**
         * Represents the found pages in a search query listing on Wikipedia
         */
        @Serializable
        data class Query(
            /**
             * Pages found by the query
             */
            val pages: Map<Int, Page>
        )

        /**
         * Represents the result of a search query on Wikipedia
         */
        @Serializable
        data class Result(
            /**
             * Query results
             */
            val query: Query
        )
    }

    /**
     * Tries to find an article from a search
     *
     * @param query the search query
     */
    override suspend fun getArticle(query: String): WikiArticle? = try {
        val apiBase = "https://${languageCode.encodeURLPath()}.wikipedia.org/w/api.php"

        val result = client.get<Result> {
            url.takeFrom(apiBase)
            parameter("action", "query")
            parameter("format", "json")
            parameter("prop", "extracts|info|pageimages")
            parameter("titles", query)
            parameter("redirects", "1")
            parameter("explaintext", "1")
            parameter("exlimit", "1")
            parameter("exchars", "500")
            parameter("inprop", "url")
            parameter("piprop", "thumbnail")
        }

        result.query.pages.entries.firstOrNull()?.let { (id, page) ->
            if (id != INVALID_PAGE_ID) WikiArticle(
                page.title,
                page.extract,
                page.fullUrl,
                page.thumbnail?.source
            ) else null
        }
    } catch (e: ResponseException) {
        null
    } catch (e: SerializationException) {
        null
    }
}
