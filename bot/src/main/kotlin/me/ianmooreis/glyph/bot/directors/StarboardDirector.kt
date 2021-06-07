/*
 * StarboardDirector.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ianmooreis.glyph.bot.directors

import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.bot.directors.config.RedisAsync
import me.ianmooreis.glyph.bot.extensions.asPlainMention
import me.ianmooreis.glyph.shared.config.server.StarboardConfig
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.lang.Integer.min

/**
 * Manages starboards in guilds with them configured
 */
class StarboardDirector(private val redis: RedisAsync) : Director() {
    /**
     * When a message is reacted upon in a guild
     */
    override fun onGenericGuildMessageReaction(event: GenericGuildMessageReactionEvent) {
        val starboardConfig = event.guild.config.starboard

        if (!starboardConfig.enabled) return

        val starboardChannel = starboardConfig.channel?.let { event.guild.getTextChannelById(it) } ?: return
        val correctEmoteName = emojiAlias(event.reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = event.reactionEmote.isEmoji || event.reactionEmote.emote.guild == event.guild
        val channelIsNotStarboard = event.channel != starboardChannel

        if (correctEmoteName && emoteBelongsToGuild && channelIsNotStarboard) {
            launch {
                val message = event.channel.retrieveMessageById(event.messageId).await()

                // Check that the number of reactions needed has been met
                val reactionCount = message.starboardReactionCount(starboardConfig)
                val reactionThresholdMet = reactionCount >= min(1, starboardConfig.threshold)

                // Check if NSFW and if so whether or not it is allowed
                val isSafe = !message.textChannel.isNSFW || starboardChannel.isNSFW

                if (reactionThresholdMet && isSafe) {
                    if (event.reactionEmote.isEmote) {
                        message.addReaction(event.reactionEmote.emote)
                    } else {
                        message.addReaction(event.reactionEmote.emoji)
                    }.await()
                    message.sendToStarboard(reactionCount, starboardChannel)
                }
            }
        }
    }

    private suspend fun Message.starboardReactionCount(starboardConfig: StarboardConfig): Int {
        val starboardReactions = reactions.find {
            it.isCorrectEmote(starboardConfig)
        } ?: return 0

        // Check that the number of reactions needed has been met
        return starboardReactions.retrieveUsers().await().count {
            // Bots should not count, including us
            val notBot = !it.isBot
            // Prevent self-starring if disallowed
            val notIllegalSelfStar = it != author || starboardConfig.allowSelfStarring

            notBot && notIllegalSelfStar
        }
    }

    private fun MessageReaction.isCorrectEmote(starboardConfig: StarboardConfig): Boolean {
        val correctEmoteName = emojiAlias(reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = reactionEmote.isEmoji || reactionEmote.emote.guild == guild
        return correctEmoteName && emoteBelongsToGuild
    }

    private suspend fun Message.sendToStarboard(reactionCount: Int, starboardChannel: TextChannel) {
        val starboardMessageBuilder = MessageBuilder()
        val firstEmbed = embeds.getOrNull(0)
        // Set-up the base embed
        val embed = EmbedBuilder().setAuthor(author.asPlainMention, jumpUrl, author.avatarUrl)
            .setDescription(contentRaw)
            .setFooter("$reactionCount ⭐ in #${textChannel.name}", null)
            .setColor(Color.YELLOW)
            .setTimestamp(timeCreated)
        // Add images
        embed.setImage(
            attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url
            ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null
        ).setThumbnail(if (firstEmbed?.title != null) embeds.getOrNull(0)?.thumbnail?.url else null)
        // Add the contents of embeds on the original message to the starboard embed
        embeds.forEach { subEmbed ->
            val title = subEmbed.title ?: subEmbed.author?.name ?: ""
            val value = (subEmbed?.description ?: "") +
                    subEmbed.fields.joinToString("") { "\n**__${it.name}__**\n${it.value}" }
            if (value.isNotBlank()) {
                embed.addField(title, StringUtils.abbreviate(value, MessageEmbed.TITLE_MAX_LENGTH), false)
            }
        }
        starboardMessageBuilder.setEmbed(embed.build())
        // Send the starboard embed to the starboard
        val trackingKey = STARBOARD_TRACK_PREFIX + id
        val firstTry = redis.setnx(trackingKey, "PENDING").await()
        val trackedMessageId = redis.get(trackingKey).await().toLongOrNull()
        if (firstTry) {
            val starboardMessage = WebhookDirector.send(starboardChannel, starboardMessageBuilder.build())
            redis.set(trackingKey, starboardMessage.id.toString())
        } else if (trackedMessageId != null) {
            WebhookDirector.update(starboardChannel, trackedMessageId, starboardMessageBuilder.build())
        }
    }

    private fun emojiAlias(emoji: String): String {
        return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
    }

    companion object {
        private const val STARBOARD_TRACK_PREFIX = "Glyph:Starboard:"
    }
}
