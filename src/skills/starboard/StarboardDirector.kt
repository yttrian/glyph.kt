package org.yttr.glyph.skills.starboard

import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import org.yttr.glyph.Director
import org.yttr.glyph.pubsub.redis.RedisAsync

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
