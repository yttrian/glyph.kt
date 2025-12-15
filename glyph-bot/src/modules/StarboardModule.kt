package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.yttr.glyph.bot.data.ConfigStore
import org.yttr.glyph.bot.data.RedisCoroutines
import org.yttr.glyph.bot.data.ServerConfig
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

        // Check that the starboard is enabled and the emoji is correct
        if (!starboardConfig.enabled || !isCorrectEmoji(event.reaction, starboardConfig)) {
            return
        }

        // Get the channel as long as it's defined and not already the starboard
        val starboardChannel = starboardConfig.channel.takeUnless { it == event.channel.idLong }?.let {
            event.guild.getTextChannelById(it)
        }

        // Check that the starboard channel was found and that it is age appropriate to send to it
        if (starboardChannel == null || !isAgeAppropriate(event.channel, starboardChannel)) {
            return
        }

        // Get the message and count the human reactions
        val message = event.retrieveMessage().await()
        val reactions = event.reaction.retrieveUsers().await()
        val reactionCount = reactions.count { user ->
            // User is not a bot or themselves (unless allowed)
            !user.isBot && (starboardConfig.canSelfStar || user.idLong != message.author.idLong)
        }

        // If enough humans have reacted
        if (reactionCount >= starboardConfig.threshold) {
            val starboard = Starboard(starboardChannel, redis)
            val alreadyReacted = reactions.any { user -> user.idLong == event.jda.selfUser.idLong }

            // Using our own reaction as a flag, either create or update the starboard message
            if (!alreadyReacted) {
                message.addReaction(event.reaction.emoji).queue()
                starboard.send(message, reactionCount)
            } else {
                starboard.update(message, reactionCount)
            }
        }
    }

    private fun isAgeAppropriate(
        messageChannel: MessageChannelUnion,
        starboardChannel: TextChannel
    ): Boolean {
        val isNsfw = messageChannel is IAgeRestrictedChannel && messageChannel.isNSFW

        return !isNsfw || starboardChannel.isNSFW
    }

    private fun isCorrectEmoji(
        reaction: MessageReaction,
        starboardConfig: ServerConfig.Starboard
    ): Boolean = when (val emoji = reaction.emoji) {
        is UnicodeEmoji -> emoji.asCodepoints == starboardConfig.emoji
        is CustomEmoji -> emoji.id == starboardConfig.emoji
        else -> false
    }

    private suspend fun killMessage(event: GenericMessageEvent, reason: String) {
        val channel = event.channel
        if (channel !is TextChannel) return

        Starboard(channel, redis).kill(event.messageIdLong, reason)
    }
}
