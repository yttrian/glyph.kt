package org.yttr.glyph.bot.quickview

import dev.minn.jda.ktx.util.SLF4J
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.data.ServerConfig
import java.io.Closeable

/**
 * Handle extract data from websites to build relevant QuickViews
 */
abstract class QuickViewGenerator : Closeable {
    /**
     * Logger
     */
    protected val log by SLF4J

    /**
     * HTTP client for making API requests
     */
    protected val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /**
     * Regex that matches valid URLs to be processed
     */
    abstract val urlRegex: Regex

    /**
     * Generate QuickView embeds for any links found in the message
     */
    abstract suspend fun generate(event: MessageReceivedEvent, config: ServerConfig.QuickView): Flow<MessageEmbed>

    /**
     * Closes the client used by the generator
     */
    override fun close(): Unit = client.close()
}
