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
import me.ianmooreis.glyph.bot.directors.starboard.StarboardDirector.Companion.TRACKING_PREFIX
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
sealed class StarredMessage(message: Message) : Message by message {
    /**
     * A regular message to be placed on the starboard
     */
    class Alive(message: Message) : StarredMessage(message) {
        override suspend fun checkAndSend(
            starboardConfig: StarboardConfig,
            starboardChannel: TextChannel,
            redis: RedisAsync
        ): Boolean {
            val starUsers = getStarUsers(starboardConfig)

            // Check that the number of reactions needed has been met
            val reactionCount = starUsers.validStarCount
            val reactionThresholdMet = reactionCount >= Integer.min(1, starboardConfig.threshold)

            // Check if NSFW and if so whether or not it is allowed
            val isSafe = !textChannel.isNSFW || starboardChannel.isNSFW

            return if (reactionThresholdMet && isSafe) {
                sendToStarboard(redis, starUsers, starboardChannel)
            } else false
        }

        override fun buildStarboardMessage(starUsers: StarUsers): Message {
            val starboardMessageBuilder = MessageBuilder()
            val firstEmbed = embeds.getOrNull(0)

            // Set-up the base embed
            val embed = EmbedBuilder()
                .setAuthor(author.asPlainMention, jumpUrl, author.avatarUrl)
                .setDescription(contentRaw.tml)
                .setFooter("${starUsers.validStarCount} ⭐ in #${textChannel.name}", null)
                .setColor(Color.YELLOW)
                .setTimestamp(timeCreated)

            // Add images
            embed.setImage(
                attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url
                ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null
            ).setThumbnail(if (firstEmbed?.title != null) embeds.getOrNull(0)?.thumbnail?.url else null)

            // Add the contents of embeds on the original message to the starboard embed
            embeds.foldIndexed(embed.length()) { runningLength, index, subEmbed ->
                val title = subEmbed.title ?: subEmbed.author?.name ?: "Embed $index"
                val valueBuilder = StringBuilder()
                subEmbed.description?.let { valueBuilder.appendLine(it) }
                subEmbed.fields.forEach {
                    valueBuilder.appendLine("**__${it.name}__**")
                    valueBuilder.appendLine(it.value)
                }
                valueBuilder.trim()
                val value = valueBuilder.toString().tml

                val addedLength = title.length + value.length
                if (valueBuilder.isNotBlank() && runningLength + addedLength < MessageEmbed.EMBED_MAX_LENGTH_BOT) {
                    embed.addField(title, value, false)
                    runningLength + addedLength
                } else runningLength
            }

            starboardMessageBuilder.setEmbed(embed.build())

            return starboardMessageBuilder.build()
        }
    }

    /**
     * A tampered message to be permanently marked on the starboard
     */
    class Dead(message: Message) : StarredMessage(message) {
        override suspend fun checkAndSend(
            starboardConfig: StarboardConfig,
            starboardChannel: TextChannel,
            redis: RedisAsync
        ): Boolean = sendToStarboard(redis, getStarUsers(starboardConfig), starboardChannel)

        override fun buildStarboardMessage(starUsers: StarUsers): Message {
            val starboardMessageBuilder = MessageBuilder()

            // Set-up the base embed
            val embed = EmbedBuilder()
                .setAuthor(author.asPlainMention, null, author.avatarUrl)
                .setDescription("Editing messages already sent to the starboard is not allowed.")
                .setFooter("❌ in #${textChannel.name}", null)
                .setColor(Color.YELLOW)
            starboardMessageBuilder.setEmbed(embed.build())

            return starboardMessageBuilder.build()
        }
    }

    /**
     * Check if valid starring and send to starboard
     */
    abstract suspend fun checkAndSend(
        starboardConfig: StarboardConfig,
        starboardChannel: TextChannel,
        redis: RedisAsync
    ): Boolean

    /**
     * Retrieve info about the star reactions on a message
     */
    protected suspend fun getStarUsers(starboardConfig: StarboardConfig): StarUsers {
        val starboardReactions = reactions.find {
            it.isCorrectEmote(starboardConfig)
        } ?: return StarUsers(starboardConfig, this, emptyList())

        // Check that the number of reactions needed has been met
        return StarUsers(starboardConfig, this, starboardReactions.retrieveUsers().submit().await())
    }

    /**
     * Build the content of the message to place on the starboard
     */
    protected abstract fun buildStarboardMessage(starUsers: StarUsers): Message

    /**
     * String abbreviated to MessageEmbed.TEXT_MAX_LENGTH
     */
    protected val String.tml: String
        get() = StringUtils.abbreviate(this, MessageEmbed.TEXT_MAX_LENGTH)

    /**
     * Attempt to send to or update the starboard
     */
    protected suspend fun sendToStarboard(
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
            // A starboard message must be created because one does not exist
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
            // Tracking reports a linked starboard message
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
                    if (e.body.contains("Unknown Message") && !retry) {
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
            // We should not create a starboard message but there's also no tracking info because it likely expired
            else -> false
        }
    }

    private fun MessageReaction.isCorrectEmote(starboardConfig: StarboardConfig): Boolean {
        val correctEmoteName = emojiAlias(reactionEmote.name) == starboardConfig.emoji
        val emoteBelongsToGuild = reactionEmote.isEmoji || reactionEmote.emote.guild == guild
        return correctEmoteName && emoteBelongsToGuild
    }

    /**
     * Info about users who star reacted a message
     */
    protected class StarUsers(
        starboardConfig: StarboardConfig,
        starredMessage: StarredMessage,
        users: List<User>
    ) : List<User> by users {
        /**
         * Number of valid star reactions
         */
        val validStarCount: Int = count {
            // Bots should not count, including us
            val notBot = !it.isBot
            // Prevent self-starring if disallowed
            val notIllegalSelfStar = it != starredMessage.author || starboardConfig.allowSelfStarring

            notBot && notIllegalSelfStar
        }

        /**
         * Did we react as well?
         */
        val selfReacted: Boolean = contains(starredMessage.jda.selfUser)
    }

    companion object {
        // Remember for 120 days since last star
        private const val TRACKING_TTL_SECONDS = 120L * 60 * 60 * 24

        // Timeout first try lock after 30 seconds if the attempt that failed oddly
        private const val FIRST_TRY_TTL_SECONDS = 30L

        private val log = LoggerFactory.getLogger(StarboardDirector::class.java.simpleName)
    }
}
