/*
 * StarboardMessage.kt
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

import club.minnced.discord.webhook.exception.HttpException
import kotlinx.coroutines.future.await
import me.ianmooreis.glyph.bot.directors.WebhookDirector
import me.ianmooreis.glyph.bot.directors.starboard.StarboardDirector.Companion.emojiAlias
import me.ianmooreis.glyph.bot.extensions.asPlainMention
import me.ianmooreis.glyph.shared.config.server.StarboardConfig
import me.ianmooreis.glyph.shared.redis.RedisAsync
import me.ianmooreis.glyph.shared.redis.redlockLock
import me.ianmooreis.glyph.shared.redis.redlockUnlock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.PermissionException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Instant

/**
 * Represents a message with stars that can be sent to the starboard
 */
class StarredMessage(message: Message, private val starboardConfig: StarboardConfig) : Message by message {
    /**
     * Check if valid starring and send to starboard
     */
    suspend fun checkAndSend(redis: RedisAsync, starboardChannel: TextChannel): Boolean {
        val starUsers = getStarUsers()

        // Check that the number of reactions needed has been met
        val reactionCount = starUsers.validStarCount
        val reactionThresholdMet = reactionCount >= Integer.min(1, starboardConfig.threshold)

        // Check if NSFW and if so whether or not it is allowed
        val isSafe = !textChannel.isNSFW || starboardChannel.isNSFW

        return if (reactionThresholdMet && isSafe) {
            sendToStarboard(redis, starUsers, starboardChannel)
        } else false
    }

    private class StarUsers(starredMessage: StarredMessage, users: List<User>) : List<User> by users {
        val validStarCount = count {
            // Bots should not count, including us
            val notBot = !it.isBot
            // Prevent self-starring if disallowed
            val notIllegalSelfStar = it != starredMessage.author || starredMessage.starboardConfig.allowSelfStarring

            notBot && notIllegalSelfStar
        }

        val selfReacted = contains(starredMessage.jda.selfUser)
    }

    private suspend fun getStarUsers(): StarUsers {
        val starboardReactions = reactions.find {
            it.isCorrectEmote(starboardConfig)
        } ?: return StarUsers(this, emptyList())

        // Check that the number of reactions needed has been met
        return StarUsers(this, starboardReactions.retrieveUsers().submit().await())
    }

    private fun buildStarboardMessage(starUsers: StarUsers): Message {
        val starboardMessageBuilder = MessageBuilder()
        val firstEmbed = embeds.getOrNull(0)
        // Set-up the base embed
        val embed = EmbedBuilder().setAuthor(author.asPlainMention, jumpUrl, author.avatarUrl)
            .setDescription(contentRaw)
            .setFooter("${starUsers.validStarCount} ⭐ in #${textChannel.name}", null)
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

        return starboardMessageBuilder.build()
    }

    private suspend fun sendToStarboard(
        redis: RedisAsync,
        starUsers: StarUsers,
        starboardChannel: TextChannel,
        retry: Boolean = false
    ): Boolean {
        val starboardMessage = buildStarboardMessage(starUsers)

        // Send the starboard embed to the starboard
        val trackingKey = TRACKING_PREFIX + id
        val pendingToken = "PENDING-${Instant.now().toEpochMilli()}"

        /**
         * If tracking is not already set, mark it as pending so that another concurrent coroutine cannot also think
         * that it is the first attempt to post a starboard image. A timeout is added in case the bot dies before
         * completion while still hold the "lock".
         */
        val pending = redis.redlockLock(trackingKey, pendingToken, FIRST_TRY_TTL_SECONDS)
        val mustCreate = pending && (!starUsers.selfReacted || retry)

        /**
         * The tracked message id which will be valid if not currently pending.
         */
        val trackedMessageId = redis.get(trackingKey).await().toLongOrNull()

        return when {
            mustCreate -> {
                try {
                    // Send the message and retrieve the ID
                    val starboardMessageId = WebhookDirector.send(starboardChannel, starboardMessage).id
                    // Replace the pending tracking id with the acquired message ID
                    redis.setex(trackingKey, TRACKING_TTL_SECONDS, starboardMessageId.toString())
                    // Report the success of sending the first message
                    true
                } catch (e: PermissionException) {
                    // An error occurred while trying to send the message to the starboard. Couldn't get a webhook?
                    log.trace(e.stackTraceToString())
                    log.warn("Permission error sending to $starboardChannel")
                    // Unlock the tracking lock, assuming we still hold it, so another attempt later can try again.
                    redis.redlockUnlock(trackingKey, pendingToken)
                    // Report the failure to send a message
                    false
                }
            }
            trackedMessageId != null -> {
                try {
                    // Update the existing starboard message with a new one
                    WebhookDirector.update(starboardChannel, trackedMessageId, starboardMessage)
                    // Push back the expiration deadline
                    redis.expire(trackingKey, TRACKING_TTL_SECONDS)
                    // Report the failure to update the message
                    true
                } catch (e: PermissionException) {
                    // Failed to update the message, likely because we couldn't get our authorized webhook.
                    log.trace(e.stackTraceToString())
                    log.warn("Permission error updating message $trackedMessageId in $starboardChannel")
                    // Report the failure to update the message
                    false
                } catch (e: HttpException) {
                    // Failed to update the message. Was it deleted?
                    log.trace(e.stackTraceToString())
                    // If the starboard message is missing
                    if (e.code == MISSING_MESSAGE_CODE && !retry) {
                        // Delete the tracking if a retry hasn't already been started
                        redis.redlockUnlock(trackingKey, trackedMessageId.toString())
                        log.info("Starboard message $trackedMessageId is missing, recreating")
                        // Retry sending to the starboard by recreating the message
                        sendToStarboard(redis, starUsers, starboardChannel, true)
                    } else if (retry) {
                        // Not sure how this would be reached because you can only retry once and retry should send
                        log.warn("Somehow almost slipped into a retry loop. How?")
                    } else {
                        log.warn("HTTP exception updating message $trackedMessageId in $starboardChannel")
                    }
                    // Report the failure to update the message
                    false
                }
            }
            else -> false
        }
    }

    private fun MessageReaction.isCorrectEmote(starboardConfig: StarboardConfig): Boolean {
        val correctEmoteName = emojiAlias(reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = reactionEmote.isEmoji || reactionEmote.emote.guild == guild
        return correctEmoteName && emoteBelongsToGuild
    }

    companion object {
        private const val TRACKING_PREFIX = "Glyph:Starboard:"

        // Remember for 120 days since last star
        private const val TRACKING_TTL_SECONDS = 120L * 60 * 60 * 24

        // Timeout first try lock after 30 seconds if the attempt that failed oddly
        private const val FIRST_TRY_TTL_SECONDS = 30L

        // Response code when a message does not exist to edit
        private const val MISSING_MESSAGE_CODE = 404

        private val log = LoggerFactory.getLogger(StarboardDirector::class.java.simpleName)
    }
}
