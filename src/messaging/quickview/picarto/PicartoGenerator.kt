package org.yttr.glyph.messaging.quickview.picarto

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.path
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.config.server.QuickviewConfig
import org.yttr.glyph.messaging.quickview.QuickviewGenerator

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
object PicartoGenerator : QuickviewGenerator() {
    private const val API_BASE: String = "https://api.picarto.tv"

    override val urlRegex: Regex = Regex("picarto\\.tv/(\\w+)", RegexOption.IGNORE_CASE)

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> =
        if (config.picartoEnabled) findChannelNames(event.message.contentRaw).mapNotNull {
            getChannel(it)?.getEmbed()
        } else emptyFlow()

    /**
     * Attempt to find Picarto channel names from links in a message, if any
     */
    private fun findChannelNames(content: String): Flow<String> =
        urlRegex.findAll(content).asFlow().mapNotNull { it.groups[1]?.value }

    private suspend fun getChannel(name: String): Channel? = try {
        client.get {
            url.takeFrom(API_BASE).path("api", "v1", "channel", "name", name)
        }.body()
    } catch (e: ResponseException) {
        log.debug("Failed to get channel $name", e)
        null
    }
}
