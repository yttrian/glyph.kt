/*
 * SlashMessageReceivedEvent.kt
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

package org.yttr.glyph.bot.messaging

import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction

/**
 * Wraps a MessageReceivedEvent to impose slash related restrictions
 */
class SlashMessageReceivedEvent(messageReceivedEvent: MessageReceivedEvent) :
    MessageReceivedEvent(messageReceivedEvent.jda, messageReceivedEvent.responseNumber, messageReceivedEvent.message) {

    /**
     * Restricted MessageChannel
     */
    class SlashMessageChannel(channel: MessageChannel) : MessageChannel by channel {
        /**
         * Slash commands should not invoke a typing indicator
         */
        override fun sendTyping(): RestAction<Void> = NoopRestAction(jda)
    }

    /**
     * Return a SlashMessageChannel instead of the actual MessageChannel
     */
    override fun getChannel(): MessageChannel = SlashMessageChannel(super.getChannel())
}
