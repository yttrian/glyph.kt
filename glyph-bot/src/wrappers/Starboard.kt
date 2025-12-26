package org.yttr.glyph.bot.wrappers

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageEdit
import dev.minn.jda.ktx.util.SLF4J
import dev.minn.jda.ktx.util.ref
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.yttr.glyph.bot.data.RedisCoroutines
import java.awt.Color

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class Starboard(channel: TextChannel, private val redis: RedisCoroutines) {
    private val channel by channel.ref()
    private val log by SLF4J

    suspend fun send(message: Message, reactionCount: Int) {
        val webhook = ensureWebhook()
        val starboardMessage = message.buildLivingMessage(reactionCount)
        val webhookMessage = webhook.sendMessageEmbeds(starboardMessage).await()

        redis.set("Glyph:Starboard:${message.id}", webhookMessage.id)
    }

    suspend fun update(message: Message, reactionCount: Int) {
        val webhook = ensureWebhook()
        val starboardMessage = message.buildLivingMessage(reactionCount)
        val webhookMessageId = redis.get("Glyph:Starboard:${message.id}") ?: return
        webhook.editMessageEmbedsById(webhookMessageId, starboardMessage).await()
    }

    suspend fun kill(messageId: Long, reason: String) {
        val webhook = ensureWebhook()
        val starredMessage = MessageEdit { content = reason }
        val webhookMessageId = redis.getdel("Glyph:Starboard:$messageId") ?: return
        webhook.editMessageById(webhookMessageId, starredMessage).await()
        redis.del("Glyph:Starboard:$messageId")
    }

    private fun Message.buildLivingMessage(reactionCount: Int) = Embed {
        author {
            name = author.name
            iconUrl = author.effectiveAvatarUrl
            url = jumpUrl
        }

        // TODO: Check max length
        description = contentRaw
        color = Color.YELLOW.rgb
        timestamp = timeCreated

        image = attachments.firstOrNull { it.isImage }?.url

        // TODO: Handle embeds

        footer {
            name = "$reactionCount ⭐ in #${channel.name}"
        }
    }

    private suspend fun ensureWebhook(): Webhook {
        val webhooks = channel.retrieveWebhooks().await()
        val webhook = webhooks.firstOrNull { it.name == WEBHOOK_NAME }

        // TODO: Handle permission errors
        return webhook ?: channel.createWebhook(WEBHOOK_NAME).await()
    }

    companion object {
        private const val WEBHOOK_NAME = "Glyph Starboard"
    }
}
