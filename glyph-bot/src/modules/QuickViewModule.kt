package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.util.SLF4J
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import org.yttr.glyph.bot.data.ConfigStore
import org.yttr.glyph.bot.data.RedisCoroutines
import org.yttr.glyph.bot.data.ServerConfig
import org.yttr.glyph.bot.quickview.furaffinity.FurAffinityGenerator

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class QuickViewModule(
    private val redis: RedisCoroutines,
    private val configStore: ConfigStore,
    private val generators: List<FurAffinityGenerator> = listOf(FurAffinityGenerator)
) : Module {
    private val log by SLF4J

    override fun boot(jda: JDA) {
        // Try to make QuickViews for any human message
        jda.listener<MessageReceivedEvent> { event ->
            if (!event.author.isBot && !event.author.isSystem && !event.isWebhookMessage) {
                makeQuickViews(event)
            }
        }

        // Delete replies when the original message is deleted
        jda.listener<MessageDeleteEvent> { event ->
            if (event.isFromGuild) {
                redis.getdel("Glyph:QuickView:${event.messageId}")?.let { replyId ->
                    event.channel.deleteMessageById(replyId).queue()
                }
            }
        }
    }

    private suspend fun makeQuickViews(event: MessageReceivedEvent) {
        val quickViews = generateQuickViews(event)

        quickViews.take(count = EMBED_LIMIT).fold(initial = event.message) { message, newEmbed ->
            if (message == event.message) {
                message.trySuppressEmbeds()
                event.message.replyEmbeds(newEmbed).mentionRepliedUser(false).await().also { reply ->
                    redis.set("Glyph:QuickView:${message.id}", reply.id)
                }
            } else {
                message.editMessageEmbeds(message.embeds + newEmbed).await()
            }
        }
    }

    private suspend fun Message.trySuppressEmbeds() {
        try {
            suppressEmbeds(true).await()
        } catch (e: PermissionException) {
            log.debug("Unable to suppress embeds for QuickViews in $channelId", e)
        }
    }

    private fun generateQuickViews(event: MessageReceivedEvent) = flow {
        val config = when {
            event.isFromGuild -> configStore.getConfig(event.guild).getQuickViewConfig()
            else -> ServerConfig.QuickView()
        }

        for (generator in generators) {
            emitAll(generator.generate(event, config))
        }
    }

    companion object {
        private const val EMBED_LIMIT = 5
    }
}
