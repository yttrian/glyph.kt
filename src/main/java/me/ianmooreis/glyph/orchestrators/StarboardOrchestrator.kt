package me.ianmooreis.glyph.orchestrators

import com.vdurmont.emoji.EmojiParser
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.SelfUser
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
                //Prevent self-starring if disallowed
                if (message.author == event.user && !starboardConfig.allowSelfStarring && message.author != event.jda.selfUser) {
                    event.reaction.removeReaction(event.user).queue()
                    return@queue
                }
                //Check whether the message should be sent to the starboard, with no duplicates and not a starboard of a starboard
                val thresholdMet = (message.reactions.findLast { emojiAlias(it.reactionEmote.name) == starboardConfig.emoji }?.count ?: 0) >= starboardConfig.threshold
                val isStarboarded = message.reactions.findLast { emojiAlias(it.reactionEmote.name) == starboardConfig.emoji }?.users?.contains(event.jda.selfUser) ?: false
                val isStarboard = if (message.embeds.size > 0) message.embeds[0].footer.text.contains("Starboard") else false
                if (thresholdMet && !isStarboarded && !isStarboard) {
                    //Mark the message as starboarded and send it to the starboard
                    when (event.reactionEmote.emote) {
                        null -> message.addReaction(event.reactionEmote.name).queue { sendToStarboard(message, event.jda.selfUser, starboardConfig.webhook) }
                        else -> message.addReaction(event.reactionEmote.emote).queue { sendToStarboard(message, event.jda.selfUser, starboardConfig.webhook) }
                    }
                }
            }
        }
    }

    private fun sendToStarboard(message: Message, selfUser: SelfUser, webhook: String) {
        val firstEmbed = message.embeds.getOrNull(0)
        //Set-up the base embed
        val embed = EmbedBuilder().setAuthor(message.author.asPlainMention, null, message.author.avatarUrl)
                .setDescription(message.contentRaw)
                .setFooter("Starboard | ${message.id} in #${message.textChannel.name}", null)
                .setColor(Color.YELLOW)
                .setTimestamp(message.creationTime)
        //Add images if not NSFW
        if (!message.textChannel.isNSFW) {
            embed.setImage(message.attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null)
                    .setThumbnail(if (firstEmbed?.title != null) message.embeds.getOrNull(0)?.thumbnail?.url else null)
        }
        //Add the contents of embeds on the original message to the starboard embed
        message.embeds.forEach {
            val title = it.title ?: it.author?.name ?: "No title"
            val value = ((it.description ?: "No description") + it.fields.joinToString("") { "\n**__${it.name}__**\n${it.value}" })
            if (title != "No title" && value != "No description") {
                embed.addField(title, if (value.length < 1024) value else "${value.substring(0..1020)}...",false)
            }
        }
        //Send the starboard embed to the starboard
        WebhookOrchestrator.send(selfUser, webhook, embed.build())
    }

    private fun emojiAlias(emoji: String): String {
        return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
    }
}