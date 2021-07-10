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

package org.yttr.glyph.bot.directors.starboard

import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import org.yttr.glyph.bot.Director
import org.yttr.glyph.shared.redis.RedisAsync

/**
 * Manages starboards in guilds with them configured
 */
class StarboardDirector(private val redis: RedisAsync) : Director() {
    /**
     * When a message is reacted upon in a guild
     */
    override fun onGenericGuildMessageReaction(event: GenericGuildMessageReactionEvent) {
        val starboardConfig = event.guild.config.starboard

        if (!starboardConfig.enabled || event.user?.isBot == true) return

        val starboardChannel = event.guild.getStarboardChannel() ?: return
        val correctEmoteName = emojiAlias(event.reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = event.reactionEmote.isEmoji || event.reactionEmote.emote.guild == event.guild
        val channelIsNotStarboard = event.channel != starboardChannel

        if (correctEmoteName && emoteBelongsToGuild && channelIsNotStarboard) {
            launch {
                val message = event.channel.retrieveMessageById(event.messageId).await()
                val successful = StarredMessage.Alive(message).checkAndSend(starboardConfig, starboardChannel, redis)

                if (successful) {
                    if (event.reactionEmote.isEmote) {
                        message.addReaction(event.reactionEmote.emote)
                    } else {
                        message.addReaction(event.reactionEmote.emoji)
                    }.queue()
                }
            }
        }
    }

    /**
     * When a message is deleted, check if there's an associated starboard message to mark as deleted
     */
    override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
        launch { killMessage(event, "Original message was deleted.") }
    }

    /**
     * When a message is edited, check if there's an associated starboard message to mark as edited
     */
    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        launch { killMessage(event, "Original message was edited.") }
    }

    private suspend fun killMessage(event: GenericGuildMessageEvent, reason: String) {
        val trackingKey = TRACKING_PREFIX + event.messageId
        if (redis.exists(trackingKey).await() == 0L) return
        val starboardChannel = event.guild.getStarboardChannel() ?: return
        StarredMessage.Dead(event, reason).checkAndSend(event.guild.config.starboard, starboardChannel, redis)
        redis.del(trackingKey)
    }

    private fun Guild.getStarboardChannel() = config.starboard.channel?.let { getTextChannelById(it) }

    companion object {
        /**
         * Redis key prefix for starboard tracking
         */
        const val TRACKING_PREFIX: String = "Glyph:Starboard:"

        /**
         * Parse an emoji to its name
         */
        fun emojiAlias(emoji: String): String {
            return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
        }
    }
}
