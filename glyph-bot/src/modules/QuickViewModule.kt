package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.data.ConfigStore
import org.yttr.glyph.bot.data.ServerConfig
import org.yttr.glyph.bot.quickview.furaffinity.FurAffinityGenerator

class QuickViewModule(private val configStore: ConfigStore) : Module {
    private val log by SLF4J
    private val generators = listOf(FurAffinityGenerator)

    override fun boot(jda: JDA) {
        jda.listener<MessageReceivedEvent> { event ->
            if (!event.author.isBot && !event.author.isSystem && !event.isWebhookMessage) {
                makeQuickViews(event)
            }
        }
    }

    private suspend fun makeQuickViews(event: MessageReceivedEvent) {
        val quickViews = generateQuickViews(event)

        quickViews.take(count = EMBED_LIMIT).fold(initial = event.message) { message, newEmbed ->
            if (message == event.message) {
                message.trySuppressEmbeds()
                event.message.replyEmbeds(newEmbed).mentionRepliedUser(false).await()
                // TODO: Readd volatile support
            } else {
                message.editMessageEmbeds(message.embeds + newEmbed).await()
            }
        }
    }

    private fun Message.trySuppressEmbeds() {
        suppressEmbeds(true).queue(null) { exception ->
            log.debug("Unable to suppress embeds for QuickViews in $channelId", exception)
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
