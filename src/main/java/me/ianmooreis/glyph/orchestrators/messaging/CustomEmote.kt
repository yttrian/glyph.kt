/*
 * CustomEmote.kt
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

package me.ianmooreis.glyph.orchestrators.messaging

import net.dv8tion.jda.core.entities.Emote

@Suppress("KDocMissingDocumentation")
enum class CustomEmote(val emote: Emote?) {
    XMARK(MessagingOrchestrator.getCustomEmote("xmark")),
    NOMARK(MessagingOrchestrator.getCustomEmote("empty")),
    CHECKMARK(MessagingOrchestrator.getCustomEmote("checkmark")),
    BOT(MessagingOrchestrator.getCustomEmote("bot")),
    DOWNLOAD(MessagingOrchestrator.getCustomEmote("download")),
    DOWNLOADING(MessagingOrchestrator.getCustomEmote("downloading")),
    LOADING(MessagingOrchestrator.getCustomEmote("loading")),
    TYPING(MessagingOrchestrator.getCustomEmote("typing")),
    ONLINE(MessagingOrchestrator.getCustomEmote("online")),
    STREAMING(MessagingOrchestrator.getCustomEmote("streaming")),
    AWAY(MessagingOrchestrator.getCustomEmote("away")),
    DND(MessagingOrchestrator.getCustomEmote("dnd")),
    OFFLINE(MessagingOrchestrator.getCustomEmote("offline")),
    INVISIBLE(MessagingOrchestrator.getCustomEmote("invisible")),
    THINKING(MessagingOrchestrator.getCustomEmote("thinking")),
    COOL(MessagingOrchestrator.getCustomEmote("cool")),
    EXPLICIT(MessagingOrchestrator.getCustomEmote("explicit")),
    CONFIDENTIAL(MessagingOrchestrator.getCustomEmote("confidential")),
    GRIMACE(MessagingOrchestrator.getCustomEmote("grimace")),
    MINDBLOWN(MessagingOrchestrator.getCustomEmote("mindblown"));

    override fun toString(): String = emote?.asMention ?: ""
}