/*
 * AuditingConfig.kt
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
 * A configuration for auditing
 */
data class AuditingConfig(
    /**
     * Whether or not to audit member joins
     */
    var joins: Boolean = false,
    /**
     * Whether or not to audit member leaves
     */
    var leaves: Boolean = false,
    /**
     * Whether or not to purges
     */
    var purge: Boolean = false,
    /**
     * Whether or not to audit kicks
     */
    var kicks: Boolean = false,
    /**
     * Whether or not to audit bans
     */
    var bans: Boolean = false,
    /**
     * Whether or not to audit username changes
     */
    var names: Boolean = false,
    /**
     * The Discord webhook to send audits to
     */
    var webhook: String? = null
) : Config {
    override fun dumpMicroConfig(guild: Guild): MicroConfig {
        return MicroConfigBuilder()
            .addValue(joins, leaves, purge, kicks, bans, names)
            .addWebhookValue(webhook)
            .build()
    }

    override fun loadMicroConfig(guild: Guild, microConfig: MicroConfig) {
        val booleans = microConfig.getBooleans(0)
        joins = booleans[0]
        leaves = booleans[1]
        purge = booleans[2]
        kicks = booleans[3]
        bans = booleans[4]
        names = booleans[5]

        webhook = microConfig.getWebhook(1, guild)
    }
}