package org.yttr.glyph.skills.wiki

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPath
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Grabs articles from Fandom wikis
 */
class FandomExtractor(
    /**
     * The Fandom wiki to search
     */
    wiki: String,
    /**
     * The minimum acceptable article quality
     */
    private val minimumQuality: Int
) : WikiExtractor() {
    private val urlBase = "https://${wiki.encodeURLPath()}.fandom.com"
    private val apiBase = "$urlBase/api/v1"

    companion object {
        /**
         * http://community.wikia.com/wiki/Help:Namespaces
         */
        const val NAMESPACES: String = "0,14"

        /**
         * The maximum length Fandom allows asking for
         */
        const val MAX_ABSTRACT_LENGTH: String = "500"

        /**
         * Represents an item from a Fandom search
         */
        @Serializable
        data class SearchResult(
            /**
             * Identity of search result
             */
            val id: Int
        )

        /**
         * Represents a Fandom search result listing
         */
        @Serializable
        data class SearchListing(
            /**
             * Search result items
             */
            val items: List<SearchResult>
        )

        /**
         * Represents a Fandom page
         */
        @Serializable
        data class Page(
            /**
             * Title of the page
             */
            val title: String,
            /**
             * URL of the page
             */
            val url: String,
            /**
             * Abstract of the page
             */
            val abstract: String,
            /**
             * URL of the thumbnail for the page
             */
            val thumbnail: String
        )

        /**
         * Represents the result of a details listing
         */
        @Serializable
        data class DetailsListing(
            /**
             * Detail result items
             */
            val items: Map<Int, Page>
        )
    }

    /**
     * Tries to grab an article from a search on a wiki
     *
     * @param query the search query
     */
    override suspend fun getArticle(query: String): WikiArticle? = try {
        val searchResult = client.get {
            url.takeFrom("$apiBase/Search/List")
            parameter("query", query)
            parameter("limit", "1")
            parameter("minArticleQuality", minimumQuality.toString())
            parameter("batch", "1")
            parameter("namespaces", NAMESPACES)
        }.body<SearchListing>()

        searchResult.items.firstOrNull()?.let {
            val page = client.get {
                url.takeFrom("$apiBase/Articles/Details")
                parameter("ids", it.id)
                parameter("abstract", MAX_ABSTRACT_LENGTH)
            }.body<DetailsListing>().items[it.id]

            if (page != null) WikiArticle(
                page.title,
                page.abstract,
                urlBase + page.url,
                page.thumbnail
            ) else null
        }
    } catch (e: ResponseException) {
        e.printStackTrace()
        null
    } catch (e: SerializationException) {
        null
    }
}
