package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.messaging.MessagingOrchestrator
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Reply to a message
 *
 * @param content the reply body
 * @param embed an embed to include in the message
 * @param deleteAfterDelay how long to wait before automatically deleting the message (if ever)
 * @param deleteAfterUnit the time units the deleteAfterDelay used
 * @param deleteWithEnabled whether or not to delete the response when the invoking message is deleted
 */
fun Message.reply(content: String? = null, embed: MessageEmbed? = null, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS, deleteWithEnabled: Boolean = true) {
    if (content == null && embed == null) {
        return
    }
    val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
    try {
        this.channel.sendMessage(message).queue {
            if (deleteAfterDelay > 0) {
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            } else if (deleteWithEnabled) {
                MessagingOrchestrator.amendLedger(this.idLong, it.idLong)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessagingOrchestrator.logSendFailure(this.textChannel)
    }
}

/**
 * Reply to a message with an embed
 *
 * @param embed the embed to send
 * @param deleteAfterDelay how long to wait before automatically deleting the message (if ever)
 * @param deleteAfterUnit the time units the deleteAfterDelay used
 * @param deleteWithEnabled whether or not to delete the response when the invoking message is deleted
 */
fun Message.reply(embed: MessageEmbed, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS, deleteWithEnabled: Boolean = true) {
    this.reply(content = null, embed = embed, deleteAfterDelay = deleteAfterDelay, deleteAfterUnit = deleteAfterUnit, deleteWithEnabled = deleteWithEnabled)
}

/**
 * Removes the @mention prefix from a content stripped message and trims any extra whitespace
 */
val Message.contentClean: String
    get() = if (this.channelType.isGuild) {
        this.contentStripped.removePrefix("@${this.guild.selfMember.effectiveName}").trim()
    } else {
        this.contentStripped.removePrefix("@${this.jda.selfUser.name}").trim()
    }

/**
 * Removes the self member from the mentioned members list
 */
val Message.cleanMentionedMembers: List<Member>
    get() = this.mentionedMembers.filter { it != this.guild.selfMember }

/**
 * Removes the self user from the mentioned users list
 */
val Message.cleanMentionedUsers: List<User>
    get() = this.mentionedUsers.filter { it != this.jda.selfUser }

/**
 * Retrieves all messages since a date in the past from the iterable history
 *
 * @param time a time in the past
 *
 * @return a list of messages since the date in the past
 */
fun TextChannel.getMessagesSince(time: OffsetDateTime): List<Message> {
    return this.iterableHistory.takeWhile { it.creationTime.isAfter(time) }
}