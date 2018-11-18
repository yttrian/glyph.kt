/*
 * SelectableRolesConfig.kt
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

package me.ianmooreis.glyph.directors.config

import net.dv8tion.jda.core.entities.Guild

/**
 * A configuration for selectable roles
 */
data class SelectableRolesConfig(
    /**
     * The list of selectable roles
     */
    val roles: List<String?> = emptyList(),
    /**
     * How many selectable roles a member can have at once
     */
    val limit: Int = 1
) : Config() {
    override fun getMicroConfig(guild: Guild): MicroConfig {
        val microConfigBuilder = MicroConfigBuilder()
        microConfigBuilder.addValue(limit)

        val selfPosition = guild.selfMember.roles.maxBy { it.position }?.position ?: 0
        val guildRoles = guild.roles
        val ghostRoles = roles.filter { guild.getRolesByName(it, false).firstOrNull() === null }

        // Iterate through every role except @everyone
        guildRoles.forEach { role ->
            val available = role.position < selfPosition
            val selected = roles.contains(role.name)
            val status = when {
                role.isPublicRole -> RoleStatus.IGNORE
                role.isManaged -> RoleStatus.IGNORE
                available && !selected -> RoleStatus.UNSELECTED
                available && selected -> RoleStatus.SELECTED
                !available && !selected -> RoleStatus.ILLEGAL_UNSELECTED
                !available && selected -> RoleStatus.ILLEGAL_SELECTED
                else -> RoleStatus.UNSELECTED
            }
            if (status !== RoleStatus.IGNORE) {
                microConfigBuilder.addValue(status.ordinal)
                microConfigBuilder.addValue(role.name)
            }
        }
        // Add roles that are still in the config but no longer exist
        ghostRoles.forEach {
            microConfigBuilder.addValue(RoleStatus.GHOST.ordinal)
            microConfigBuilder.addValue(it)
        }

        return microConfigBuilder.build()
    }

    /**
     * Specified the status of a role
     */
    enum class RoleStatus {
        /**
         * Not selected currently
         */
        UNSELECTED,
        /**
         * Currently selected
         */
        SELECTED,
        /**
         * Cannot be selected, and is not currently selected
         */
        ILLEGAL_UNSELECTED,
        /**
         * Cannot be selected, but is currently selected
         */
        ILLEGAL_SELECTED,
        /**
         * The role no longer exists by the name
         */
        GHOST,
        /**
         * Should be completely ignored
         */
        IGNORE
    }
}