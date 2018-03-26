package me.ianmooreis.glyph.orchestrators

import com.vdurmont.emoji.EmojiParser
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color

object StarboardOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        val starboardConfig = event.guild.config.starboard
        val emojiName = emojiAlias(event.reactionEmote.name)
        if (starboardConfig.enabled && emojiName == starboardConfig.emoji && starboardConfig.webhook != null) {
            event.channel.getMessageById(event.messageId).queue { message ->
                val duplicate = (message.reactions.findLast { emojiAlias(it.reactionEmote.name) == starboardConfig.emoji }?.count ?: 0) > 1
                if (!duplicate) {
                    when (event.reactionEmote.emote) {
                        null -> message.addReaction(event.reactionEmote.name).queue()
                        else -> message.addReaction(event.reactionEmote.emote).queue()
                    }
                    val firstEmbed = message.embeds.getOrNull(0)
                    val embed = EmbedBuilder().setAuthor(message.author.asPlainMention, null, message.author.avatarUrl)
                            .setDescription(message.contentRaw)
                            .setImage(message.attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null)
                            .setThumbnail(if (firstEmbed?.title != null) message.embeds.getOrNull(0)?.thumbnail?.url else null)
                            .setFooter("Starboard | ${message.id} in #${message.textChannel.name}", null)
                            .setColor(Color.YELLOW)
                            .setTimestamp(message.creationTime)
                    message.embeds.forEach {
                        val title = it.title ?: it.author?.name ?: "No title"
                        val value = ((it.description ?: "No description") + it.fields.joinToString("") { "\n**__${it.name}__**\n${it.value}" })
                        if (title != "No title" && value != "No description") {
                            embed.addField(title, if (value.length < 1024) value else "${value.substring(0..1020)}...",false)
                        }
                    }
                    WebhookOrchestrator.send(event.jda.selfUser, starboardConfig.webhook, embed.build())
                }
            }
        }
    }

    private fun emojiAlias(emoji: String): String {
        return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
    }
}