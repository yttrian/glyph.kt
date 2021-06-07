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

package me.ianmooreis.glyph.bot.directors.starboard

import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.launch
import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.shared.redis.RedisAsync
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent

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
                val sent = StarredMessage(message, starboardConfig).checkAndSend(redis, starboardChannel)

                if (sent) {
                    if (event.reactionEmote.isEmote) {
                        message.addReaction(event.reactionEmote.emote)
                    } else {
                        message.addReaction(event.reactionEmote.emoji)
                    }.queue()
                }
            }
        }
    }

    companion object {
        fun emojiAlias(emoji: String): String {
            return EmojiParser.parseToAliases(emoji).removeSurrounding(":")
        }
    }
}
