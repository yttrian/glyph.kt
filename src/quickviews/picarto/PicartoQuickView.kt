package org.yttr.glyph.quickviews.picarto

import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.yttr.glyph.quickviews.QuickView

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
data class PicartoQuickView(val channel: String) : QuickView {
    override suspend fun build(): EmbedBuilder? = try {
        client.get {
            url.path("channel/name/$channel")
        }.body<Channel>().getEmbed()
    } catch (e: ResponseException) {
        log.debug("Failed to get channel $channel", e)
        null
    }

    companion object : QuickView.Scanner<PicartoQuickView> {
        private val log = LoggerFactory.getLogger(PicartoQuickView::class.java)

        private val client = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            defaultRequest {
                url("https://api.picarto.tv/api/v1")
            }
        }

        private val urlRegex: Regex = Regex("picarto\\.tv/(\\w+)", RegexOption.IGNORE_CASE)

        private fun findChannelNames(content: String): List<String> =
            urlRegex.findAll(content).mapNotNull { it.groups[1]?.value }.toList()

        override fun scan(message: Message): List<PicartoQuickView> {
            return findChannelNames(message.content).map { PicartoQuickView(it) }
        }
    }
}
