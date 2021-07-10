/*
 * UserGuild.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package me.ianmooreis.glyph.config.discord

/**
 * Represents a users relationship to a guild
 */
data class UserGuild(
    /**
     * The snowflake id of the guild
     */
    val id: String,
    /**
     * The name of the guild
     */
    val name: String,
    private val permissions: Int
) {
    /**
     * Whether or not the user has manage guild permission
     */
    val hasManageGuild: Boolean
        get() = permissions.and(PERMISSION_MANAGE_GUILD) == PERMISSION_MANAGE_GUILD

    companion object {
        private const val PERMISSION_MANAGE_GUILD: Int = 0x00000020
    }
}
