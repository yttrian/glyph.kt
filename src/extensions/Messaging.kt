/*
 * Messaging.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.extensions

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
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
fun Message.reply(
    content: String? = null,
    embed: MessageEmbed? = null,
    deleteAfterDelay: Long = 0,
    deleteAfterUnit: TimeUnit = TimeUnit.SECONDS,
    deleteWithEnabled: Boolean = true
) {
    if (content == null && embed == null) {
        return
    }
    val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
    try {
        this.channel.sendMessage(message).queue {
            if (deleteAfterDelay > 0) {
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            } else if (deleteWithEnabled) {
                //MessagingDirector.amendLedger(this.idLong, it.idLong)
            }
        }
    } catch (e: InsufficientPermissionException) {
        //MessagingDirector.logSendFailure(this.textChannel)
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
fun Message.reply(
    embed: MessageEmbed,
    deleteAfterDelay: Long = 0,
    deleteAfterUnit: TimeUnit = TimeUnit.SECONDS,
    deleteWithEnabled: Boolean = true
) {
    this.reply(
        content = null,
        embed = embed,
        deleteAfterDelay = deleteAfterDelay,
        deleteAfterUnit = deleteAfterUnit,
        deleteWithEnabled = deleteWithEnabled
    )
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
    return this.iterableHistory.takeWhile { it.timeCreated.isAfter(time) }
}