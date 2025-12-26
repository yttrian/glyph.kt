package org.yttr.glyph.bot.wrappers

///**
// * Represents a message with stars that can be sent to the starboard
// */
//sealed class StarredMessage(private val messageId: Long) {
//    /**
//     * A regular message to be placed on the starboard
//     */
//    class Alive(message: Message) : StarredMessage(message.idLong) {
//        override suspend fun checkAndSend(
//            starboardConfig: ServerConfig.Starboard,
//            starboardChannel: TextChannel,
//            redis: RedisAsync
//        ): Boolean = coroutineScope {
//            val starUsers = getStarUsers(starboardConfig)
//
//            // Check that the number of reactions needed has been met
//            val reactionCount = starUsers.validStarCount
//            val reactionThresholdMet = reactionCount >= starboardConfig.threshold.coerceAtLeast(1)
//
//            // Check if NSFW and if so whether or not it is allowed
//            val isSafe = !textChannel.isNSFW || starboardChannel.isNSFW
//
//            if (userAllowsStarboarding.await() && reactionThresholdMet && isSafe) {
//                sendToStarboard(redis, starUsers, starboardChannel)
//            } else false
//        }
//
//        override fun buildStarboardMessage(starUsers: StarUsers): Message {
//            val starboardMessageBuilder = MessageBuilder()
//            val firstEmbed = embeds.getOrNull(0)
//
//            // Set-up the base embed
//            val embed = EmbedBuilder()
//                .setAuthor(author.asPlainMention, jumpUrl, author.avatarUrl)
//                .setDescription(contentRaw.tml)
//                .setFooter("${starUsers.validStarCount} ⭐ in #${textChannel.name}", null)
//                .setColor(Color.YELLOW)
//                .setTimestamp(timeCreated)
//
//            // Add images
//            embed.setImage(
//                attachments.getOrNull(0)?.url ?: firstEmbed?.image?.url
//                ?: if (firstEmbed?.title == null) firstEmbed?.thumbnail?.url else null
//            ).setThumbnail(if (firstEmbed?.title != null) embeds.getOrNull(0)?.thumbnail?.url else null)
//
//            // Add the contents of embeds on the original message to the starboard embed
//            embeds.foldIndexed(embed.length()) { runningLength, index, subEmbed ->
//                val title = subEmbed.title ?: subEmbed.author?.name ?: "Embed $index"
//                val valueBuilder = StringBuilder()
//                subEmbed.description?.let { valueBuilder.appendLine(it) }
//                subEmbed.fields.forEach {
//                    valueBuilder.appendLine("**__${it.name}__**")
//                    valueBuilder.appendLine(it.value)
//                }
//                valueBuilder.trim()
//                val value = valueBuilder.toString().tml
//
//                val addedLength = title.length + value.length
//                if (valueBuilder.isNotBlank() && runningLength + addedLength < MessageEmbed.EMBED_MAX_LENGTH_BOT) {
//                    embed.addField(title, value, false)
//                    runningLength + addedLength
//                } else runningLength
//            }
//
//            starboardMessageBuilder.setEmbed(embed.build())
//
//            return starboardMessageBuilder.build()
//        }
//    }
//
//    /**
//     * A tampered message to be permanently marked on the starboard
//     */
//    class Dead(event: GenericGuildMessageEvent, private val reason: String) : StarredMessage(event.messageIdLong) {
//        private val textChannel = event.channel
//
//        override suspend fun checkAndSend(
//            starboardConfig: StarboardConfig,
//            starboardChannel: TextChannel,
//            redis: RedisAsync
//        ): Boolean = sendToStarboard(redis, StarUsers.Fake, starboardChannel, true)
//
//        override fun buildStarboardMessage(starUsers: StarUsers): Message {
//            val starboardMessageBuilder = MessageBuilder()
//
//            // Set-up the base embed
//            val embed = EmbedBuilder()
//                .setDescription(reason)
//                .setFooter("❌ in #${textChannel.name}", null)
//                .setColor(Color.YELLOW)
//            starboardMessageBuilder.setEmbed(embed.build())
//
//            return starboardMessageBuilder.build()
//        }
//    }
//
//    /**
//     * Check if valid starring and send to starboard
//     */
//    abstract suspend fun checkAndSend(
//        starboardConfig: ServerConfig.Starboard,
//        starboardChannel: TextChannel,
//        redis: RedisAsync
//    ): Boolean
//
//    /**
//     * Retrieve info about the star reactions on a message
//     */
//    protected suspend fun Message.getStarUsers(starboardConfig: StarboardConfig): StarUsers {
//        val starboardReactions = reactions.find {
//            it.isCorrectEmote(starboardConfig)
//        } ?: return StarUsers.Real(starboardConfig, this, emptyList())
//
//        // Check that the number of reactions needed has been met
//        return StarUsers.Real(starboardConfig, this, starboardReactions.retrieveUsers().submit().await())
//    }
//
//    /**
//     * Build the content of the message to place on the starboard
//     */
//    protected abstract fun buildStarboardMessage(starUsers: StarUsers): Message
//
//    /**
//     * String abbreviated to MessageEmbed.TEXT_MAX_LENGTH
//     */
//    protected val String.tml: String
//        get() = when {
//            this.length < MessageEmbed.TEXT_MAX_LENGTH -> this
//            else -> this.substring(startIndex = 0, endIndex = MessageEmbed.TEXT_MAX_LENGTH - 3) + "..."
//        }
//
//    /**
//     * Attempt to send to or update the starboard
//     */
//    protected suspend fun sendToStarboard(
//        redis: RedisAsync,
//        starUsers: StarUsers,
//        starboardChannel: TextChannel,
//        retry: Boolean = false
//    ): Boolean {
//        val starboardMessage = buildStarboardMessage(starUsers)
//
//        // Send the starboard embed to the starboard
//        val trackingKey = TRACKING_PREFIX + messageId
//        val pendingToken = "PENDING-${Instant.now().toEpochMilli()}"
//
//        /**
//         * If tracking is not already set, mark it as pending so that another concurrent coroutine cannot also think
//         * that it is the first attempt to post a starboard image. A timeout is added in case the bot dies before
//         * completion while still hold the "lock".
//         */
//        val pending = redis.redlockLock(trackingKey, pendingToken, FIRST_TRY_TTL_SECONDS)
//
//        /**
//         * If we acquired the lock (pending) and have not reacted on the message yet or are in a retry, we must create
//         * a starboard message. Our self react is used to remember a message was sent to the starboard even after Redis
//         * tracking expires. Retry is used when an edit fails to attempt to recreate, or when warning about editing to
//         * not recreate... which I guess is kind of confusing, since it depends on pending.
//         */
//        val mustCreate = pending && (!starUsers.selfReacted || retry)
//
//        /**
//         * The tracked message id which will be valid if not currently pending.
//         */
//        val trackedMessageId = redis.get(trackingKey).await().toLongOrNull()
//
//        return when {
//            // A starboard message must be created because one does not exist
//            mustCreate -> {
//                try {
//                    // Send the message and retrieve the ID
//                    val starboardMessageId = WebhookDirector.send(starboardChannel, starboardMessage).id
//                    // Replace the pending tracking id with the acquired message ID
//                    redis.setex(trackingKey, TRACKING_TTL_SECONDS, starboardMessageId.toString())
//                    // Report the success of sending the first message
//                    true
//                } catch (e: PermissionException) {
//                    // An error occurred while trying to send the message to the starboard. Couldn't get a webhook?
//                    log.trace(e.stackTraceToString())
//                    log.warn("Permission error sending to $starboardChannel")
//                    // Unlock the tracking lock, assuming we still hold it, so another attempt later can try again.
//                    redis.redlockUnlock(trackingKey, pendingToken)
//                    // Report the failure to send a message
//                    false
//                }
//            }
//            // Tracking reports a linked starboard message
//            trackedMessageId != null -> {
//                try {
//                    // Update the existing starboard message with a new one
//                    WebhookDirector.update(starboardChannel, trackedMessageId, starboardMessage)
//                    // Push back the expiration deadline
//                    redis.expire(trackingKey, TRACKING_TTL_SECONDS)
//                    // Report the failure to update the message
//                    true
//                } catch (e: PermissionException) {
//                    // Failed to update the message, likely because we couldn't get our authorized webhook.
//                    log.trace(e.stackTraceToString())
//                    log.warn("Permission error updating message $trackedMessageId in $starboardChannel")
//                    // Report the failure to update the message
//                    false
//                } catch (e: HttpException) {
//                    // Failed to update the message. Was it deleted?
//                    log.trace(e.stackTraceToString())
//                    // If the starboard message is missing
//                    if (e.body.contains("Unknown Message") && !retry) {
//                        // Delete the tracking if a retry hasn't already been started
//                        redis.redlockUnlock(trackingKey, trackedMessageId.toString())
//                        log.info("Starboard message $trackedMessageId is missing, recreating")
//                        // Retry sending to the starboard by recreating the message
//                        sendToStarboard(redis, starUsers, starboardChannel, true)
//                    } else if (retry) {
//                        // This is likely only reached when a deleted starboard message is edited
//                        log.debug("Attempted to edit on a retry but failed")
//                    } else {
//                        log.warn("HTTP exception updating message $trackedMessageId in $starboardChannel")
//                    }
//                    // Report the failure to update the message
//                    false
//                }
//            }
//            // We should not create a starboard message but there's also no tracking info because it likely expired
//            else -> false
//        }
//    }
//
//    private fun MessageReaction.isCorrectEmote(starboardConfig: StarboardConfig): Boolean {
//        val correctEmoteName = emojiAlias(reactionEmote.name) == starboardConfig.emoji
//        val emoteBelongsToGuild = reactionEmote.isEmoji || reactionEmote.emote.guild == guild
//        return correctEmoteName && emoteBelongsToGuild
//    }
//
//    /**
//     * Info about users who star reacted a message
//     */
//    protected sealed class StarUsers {
//        /**
//         * Number of valid star reactions
//         */
//        abstract val validStarCount: Int
//
//        /**
//         * Did we react as well?
//         */
//        abstract val selfReacted: Boolean
//
//        /**
//         * Real StarUsers for normal usage
//         */
//        class Real(starboardConfig: StarboardConfig, message: Message, users: List<User>) : StarUsers() {
//            override val validStarCount: Int = users.count {
//                // Bots should not count, including us
//                val notBot = !it.isBot
//                // Prevent self-starring if disallowed
//                val notIllegalSelfStar = it != message.author || starboardConfig.allowSelfStarring
//
//                notBot && notIllegalSelfStar
//            }
//
//            override val selfReacted: Boolean = users.contains(message.jda.selfUser)
//        }
//
//        /**
//         * Fake StarUsers for dead stars
//         */
//        object Fake : StarUsers() {
//            override val validStarCount: Int = 0
//            override val selfReacted: Boolean = false
//        }
//    }
//
//    companion object {
//        // Remember for 120 days since last star
//        private const val TRACKING_TTL_SECONDS = 120L * 60 * 60 * 24
//
//        // Timeout first try lock after 30 seconds if the attempt that failed oddly
//        private const val FIRST_TRY_TTL_SECONDS = 30L
//
//        private val log = LoggerFactory.getLogger(StarboardDirector::class.java.simpleName)
//    }
//}
