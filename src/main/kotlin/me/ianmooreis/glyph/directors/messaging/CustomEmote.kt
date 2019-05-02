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

/**
 * Useful custom emotes from the EMOJI_GUILD
 */
enum class CustomEmote(
    /**
     * An emote
     */
    private val emote: Emote?
) {
    /**
     * An X in a red box
     */
    XMARK(MessagingDirector.getCustomEmote("xmark")),
    /**
     * An empty box
     */
    NOMARK(MessagingDirector.getCustomEmote("empty")),
    /**
     * A checkbox
     */
    CHECKMARK(MessagingDirector.getCustomEmote("checkmark")),
    /**
     * A badge that says bot
     */
    BOT(MessagingDirector.getCustomEmote("bot")),
    /**
     * A download symbol
     */
    DOWNLOAD(MessagingDirector.getCustomEmote("download")),
    /**
     * An animated download symbol
     */
    DOWNLOADING(MessagingDirector.getCustomEmote("downloading")),
    /**
     * The Discord loading squares
     */
    LOADING(MessagingDirector.getCustomEmote("loading")),
    /**
     * The Discord typing indicator
     */
    TYPING(MessagingDirector.getCustomEmote("typing")),
    /**
     * The Discord status dot for online
     */
    ONLINE(MessagingDirector.getCustomEmote("online")),
    /**
     * The Discord status dot for streaming
     */
    STREAMING(MessagingDirector.getCustomEmote("streaming")),
    /**
     * The Discord status dot for away
     */
    AWAY(MessagingDirector.getCustomEmote("away")),
    /**
     * The Discord status dot for do not disturb
     */
    DND(MessagingDirector.getCustomEmote("dnd")),
    /**
     * The Discord status dot for offline
     */
    OFFLINE(MessagingDirector.getCustomEmote("offline")),
    /**
     * The Discord status dot for invisible
     */
    INVISIBLE(MessagingDirector.getCustomEmote("invisible")),
    /**
     * A custom thinking emote
     */
    THINKING(MessagingDirector.getCustomEmote("thinking")),
    /**
     * A custom emote of a cool face (like wearing sunglasses)
     */
    COOL(MessagingDirector.getCustomEmote("cool")),
    /**
     * A custom emote warning of explicit nature
     */
    EXPLICIT(MessagingDirector.getCustomEmote("explicit")),
    /**
     * A custom emote indicating something confidential
     */
    CONFIDENTIAL(MessagingDirector.getCustomEmote("confidential")),
    /**
     * A custom emote of a face grimacing for something bad that happened
     */
    GRIMACE(MessagingDirector.getCustomEmote("grimace")),
    /**
     * A custom emote of a mind being blow
     */
    MINDBLOWN(MessagingDirector.getCustomEmote("mindblown"));

    override fun toString(): String = emote?.asMention ?: ""
}