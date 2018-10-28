/*
 * StarboardDirector *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors

import com.vdurmont.emoji.EmojiParser
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.SelfUser
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import java.awt.Color

/**
 * Manages starboards in guilds with them configured
 */
object StarboardDirector : Director() {
    /**
     * When a message is reacted upon in a guild
     */
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        val starboardConfig = event.guild.config.starboard
        val emojiName = emojiAlias(event.reactionEmote.name)

        // Welcome to callback hell
        // TODO: Make these starboard handling/checks "shallower"
        if (starboardConfig.enabled && emojiName == starboardConfig.emoji && starboardConfig.webhook != null) {
            event.channel.getMessageById(event.messageId).queue { message ->
                // Prevent self-starring if disallowed
                if (message.author == event.user && !starboardConfig.allowSelfStarring && message.author != event.jda.selfUser) {
                    event.reaction.removeReaction(event.user).queue()
                    return@queue
                }
                // Check whether the message should be sent to the starboard, with no duplicates and not a starboard of a starboard
                val starboardReactions = message.reactions.findLast { emojiAlias(it.reactionEmote.name) == starboardConfig.emoji }
                    ?: return@queue
                val thresholdMet = (starboardReactions.count) >= starboardConfig.threshold
                // We have to make a separate request to actually see who reacted, this is new
                starboardReactions.users.queue { reactedUsers ->
                    if (!reactedUsers.contains(event.jda.selfUser)) {
                        val messageFooter = message.embeds.getOrNull(0)?.footer?.text ?: ""
                        val isStarboard = (message.isWebhookMessage && message.embeds.size > 0 && messageFooter.contains("Starboard"))
                        val send = { sendToStarboard(message, event.jda.selfUser, starboardConfig.webhook) }
                        if (thresholdMet && !isStarboard) {
                            // Mark the message as starboarded and send it to the starboard
                            when (event.reactionEmote.emote) {
                                null -> message.addReaction(event.reactionEmote.name).queue { send() }
                                else -> message.addReaction(event.reactionEmote.emote).queue { send() }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendToStarboard(message: Message, selfUser: SelfUser, webhook: String) {
        val firstEmbed = message.embeds.getOrNull(0)
        //Set-up the base embed
        val embed = EmbedBuilder().setAuthor(message.author.asPlainMention, message.jumpUrl, message.author.avatarUrl)
            .setDescription(message.contentRaw)
            .setFooter("Starboard | ${message.id} in #${message.textChannel.name}", null)
            .setColor(Color.YELLOW)
            .setTimestamp(message.creationTime)
        //Add images if not NSFW
        if (!message.textChannel.isNSFW) {
            embed.setImage(message.attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url
            ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null)
                .setThumbnail(if (firstEmbed?.title != null) message.embeds.getOrNull(0)?.thumbnail?.url else null)
        }
        //Add the contents of embeds on the original message to the starboard embed
        message.embeds.forEach { subEmbed ->
            val title = subEmbed.title ?: subEmbed.author?.name ?: ""
            val value = (subEmbed?.description ?: "") +
                subEmbed.fields.joinToString("") { "\n**__${it.name}__**\n${it.value}" }
            if (value.isNotBlank()) {
                embed.addField(title, if (value.length < 1024) value else "${value.substring(0..1020)}...", false)
            }
        }
        //Send the starboard embed to the starboard
        WebhookDirector.send(selfUser, webhook, embed.build())
    }

    private fun emojiAlias(emoji: String): String {
        return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
    }
}