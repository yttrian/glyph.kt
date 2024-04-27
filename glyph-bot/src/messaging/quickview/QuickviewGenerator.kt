package org.yttr.glyph.bot.messaging.quickview

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yttr.glyph.shared.config.server.QuickviewConfig
import java.io.Closeable

/**
 * Handle extract data from websites to build relevant QuickViews
 */
abstract class QuickviewGenerator : Closeable {
    /**
     * Logger
     */
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * HTTP client for making API requests
     */
    protected val client: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json { ignoreUnknownKeys = true })
        }
    }

    /**
     * Regex that matches valid URLs to be processed
     */
    abstract val urlRegex: Regex

    /**
     * Generate QuickView embeds for any links found in the message
     */
    abstract suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed>

    /**
     * Closes the client used by the generator
     */
    override fun close(): Unit = client.close()
}
