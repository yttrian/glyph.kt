package org.yttr.glyph.bot.modules

import com.vdurmont.emoji.EmojiParser
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.yttr.glyph.bot.skills.starboard.StarredMessage
import org.yttr.glyph.shared.config.ConfigStore
import org.yttr.glyph.shared.pubsub.redis.RedisAsync

/**
 * Manages starboards in guilds with them configured
 */
class StarboardModule(private val redis: RedisAsync, private val configStore: ConfigStore) : Module {
    override fun boot(jda: JDA) {
        jda.listener<MessageReactionAddEvent> { event ->
            if (event.isFromGuild) {
                onMessageReactionAdd(event)
            }
        }
        jda.listener<MessageDeleteEvent> { event ->
            if (event.isFromGuild) {
                killMessage(event, "The original message was deleted.")
            }
        }
        jda.listener<MessageUpdateEvent> { event ->
            if (event.isFromGuild) {
                killMessage(event, "The original message was edited.")
            }
        }
    }

    private suspend fun Guild.getStarboardConfig() = configStore.getConfig(this).starboard

    /**
     * When a message is reacted upon in a guild
     */
    suspend fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        val starboardConfig = event.guild.getStarboardConfig()

        if (!starboardConfig.enabled || event.user?.isBot == true) return

        val starboardChannel = event.guild.getStarboardChannel() ?: return
        val correctEmoteName = emojiAlias(event.reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = event.reactionEmote.isEmoji || event.reactionEmote.emote.guild == event.guild
        val channelIsNotStarboard = event.channel != starboardChannel

        if (correctEmoteName && emoteBelongsToGuild && channelIsNotStarboard) {
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

    private suspend fun killMessage(event: GenericMessageEvent, reason: String) {
        val trackingKey = TRACKING_PREFIX + event.messageId
        if (redis.exists(trackingKey).await() == 0L) return
        val config = event.guild.getStarboardConfig()
        val starboardChannel = config.channel?.let { event.guild.getTextChannelById(it) } ?: return
        StarredMessage.Dead(event, reason).checkAndSend(config, starboardChannel, redis)
        redis.del(trackingKey)
    }

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
