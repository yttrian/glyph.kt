package me.ianmooreis.glyph.orchestrators

import com.vdurmont.emoji.EmojiParser
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.time.Instant

object StarboardOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        val starboardConfig = event.guild.config.starboard
        val emojiName = EmojiParser.parseToAliases(event.reactionEmote.name).removeSurrounding(":")
        if (starboardConfig.enabled && emojiName == starboardConfig.emoji && starboardConfig.webhook != null && event.channelType.isGuild) {
            event.channel.getMessageById(event.messageId).queue { message ->
                val embed = EmbedBuilder().setAuthor(message.author.asPlainMention, null, message.author.avatarUrl)
                    .setDescription(message.contentDisplay)
                    .setImage(message.attachments.getOrNull(0)?.url ?: message.embeds.getOrNull(0)?.image?.url)
                    .setThumbnail(message.embeds.getOrNull(0)?.thumbnail?.url)
                    .setFooter("Starboard | ${message.id} in #${message.textChannel.name}", null)
                    .setColor(Color.YELLOW)
                    .setTimestamp(Instant.now())
                message.embeds.forEach {
                    val value = ((it.description ?: "") + it.fields.joinToString("") { "\n**__${it.name}__**\n${it.value}" })
                    embed.addField(it.title ?: it.author.name,
                            if (value.length < 1024) value else "${value.substring(0..1020)}...",
                            false)
                }
                WebhookOrchestrator.send(event.jda.selfUser, starboardConfig.webhook, embed.build())
            }
        }
    }
}