/*
 * FakeSlashedMessage.kt
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

package org.yttr.glyph.bot.messaging.slash

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageActivity
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.internal.entities.AbstractMessage
import java.time.OffsetDateTime
import java.util.Formatter

/**
 * Represents a fake regular message create from a slash command event
 */
class FakeSlashedMessage(private val event: SlashCommandEvent, content: String) :
    AbstractMessage(content, null, false) {
    override fun getIdLong(): Long = event.idLong

    override fun formatTo(formatter: Formatter?, flags: Int, width: Int, precision: Int) {
        TODO("Not yet implemented")
    }

    override fun getContentStripped(): String = MarkdownSanitizer.sanitize(contentRaw)

    override fun getTimeEdited(): OffsetDateTime = event.timeCreated

    override fun getAuthor(): User = event.user

    override fun getMember(): Member? = event.member

    override fun isFromType(type: ChannelType): Boolean = type == event.channelType

    override fun getChannelType(): ChannelType = event.channelType

    override fun getChannel(): MessageChannel = event.channel

    override fun getPrivateChannel(): PrivateChannel = event.privateChannel

    override fun getTextChannel(): TextChannel = event.textChannel

    override fun getGuild(): Guild = event.textChannel.guild

    override fun getActivity(): MessageActivity? = null

    override fun getJDA(): JDA = event.jda

    override fun addReaction(emote: Emote): RestAction<Void> = NoopRestAction(jda)

    override fun addReaction(unicode: String): RestAction<Void> = NoopRestAction(jda)

    /**
     * Do nothing on unsupported operations
     */
    override fun unsupported(): Unit = Unit
}
