package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.yttr.glyph.bot.data.ConfigStore
import org.yttr.glyph.bot.data.RedisCoroutines
import org.yttr.glyph.bot.starboard.Starboard

/**
 * Manages starboards in guilds with them configured
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class StarboardModule(private val redis: RedisCoroutines, private val configStore: ConfigStore) : Module {
    override fun boot(jda: JDA) {
        jda.listener<MessageReactionAddEvent> { event ->
            if (event.isFromGuild && event.user?.isBot == false) {
                checkReactionForStarboarding(event)
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

    private suspend fun Guild.getStarboardConfig() = configStore.getConfig(this).getStarboardConfig()

    /**
     * When a message is reacted upon in a guild
     */
    suspend fun checkReactionForStarboarding(event: MessageReactionAddEvent) {
        val starboardConfig = event.guild.getStarboardConfig()
        val starboardEnabled = starboardConfig.enabled && starboardConfig.channel != null

        if (!starboardEnabled || starboardConfig.channel == event.channel.idLong) {
            return
        }

        val starboardChannel = event.guild.getTextChannelById(starboardConfig.channel)
        val isCorrectEmoji = when (val emoji = event.reaction.emoji) {
            is UnicodeEmoji -> emoji.asCodepoints == starboardConfig.emoji
            is CustomEmoji -> emoji.id == starboardConfig.emoji
            else -> false
        }

        if (starboardChannel == null || !isCorrectEmoji) {
            return
        }

        val messageChannel = event.channel
        val isNsfw = messageChannel is IAgeRestrictedChannel && messageChannel.isNSFW

        if (isNsfw && !starboardChannel.isNSFW) {
            return
        }

        val message = event.retrieveMessage().await()
        val reactions = message.retrieveReactionUsers(event.reaction.emoji).await()
        val reactionCount = reactions.count { user ->
            // User is not a bot or themselves (unless allowed)
            !user.isBot && (starboardConfig.canSelfStar || user.idLong != message.author.idLong)
        }

        if (reactionCount >= starboardConfig.threshold) {
            val starboard = Starboard(starboardChannel, redis)
            val alreadyReacted = reactions.any { user -> user.idLong == event.jda.selfUser.idLong }

            if (!alreadyReacted) {
                message.addReaction(event.reaction.emoji).queue()
                starboard.send(message, reactionCount)
            } else {
                starboard.update(message, reactionCount)
            }
        }
    }

    private suspend fun killMessage(event: GenericMessageEvent, reason: String) {
        val channel = event.channel
        if (channel !is TextChannel) return

        Starboard(channel, redis).kill(event.messageIdLong)
    }
}
