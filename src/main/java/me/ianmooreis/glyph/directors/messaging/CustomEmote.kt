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

package me.ianmooreis.glyph.directors.messaging

import net.dv8tion.jda.core.entities.Emote

@Suppress("KDocMissingDocumentation")
enum class CustomEmote(val emote: Emote?) {
    XMARK(MessagingDirector.getCustomEmote("xmark")),
    NOMARK(MessagingDirector.getCustomEmote("empty")),
    CHECKMARK(MessagingDirector.getCustomEmote("checkmark")),
    BOT(MessagingDirector.getCustomEmote("bot")),
    DOWNLOAD(MessagingDirector.getCustomEmote("download")),
    DOWNLOADING(MessagingDirector.getCustomEmote("downloading")),
    LOADING(MessagingDirector.getCustomEmote("loading")),
    TYPING(MessagingDirector.getCustomEmote("typing")),
    ONLINE(MessagingDirector.getCustomEmote("online")),
    STREAMING(MessagingDirector.getCustomEmote("streaming")),
    AWAY(MessagingDirector.getCustomEmote("away")),
    DND(MessagingDirector.getCustomEmote("dnd")),
    OFFLINE(MessagingDirector.getCustomEmote("offline")),
    INVISIBLE(MessagingDirector.getCustomEmote("invisible")),
    THINKING(MessagingDirector.getCustomEmote("thinking")),
    COOL(MessagingDirector.getCustomEmote("cool")),
    EXPLICIT(MessagingDirector.getCustomEmote("explicit")),
    CONFIDENTIAL(MessagingDirector.getCustomEmote("confidential")),
    GRIMACE(MessagingDirector.getCustomEmote("grimace")),
    MINDBLOWN(MessagingDirector.getCustomEmote("mindblown"));

    override fun toString(): String = emote?.asMention ?: ""
}