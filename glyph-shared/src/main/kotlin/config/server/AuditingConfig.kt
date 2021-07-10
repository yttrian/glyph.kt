/*
 * AuditingConfig.kt
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

package org.yttr.glyph.shared.config.server

import kotlinx.serialization.Serializable
import org.yttr.glyph.shared.config.Config

/**
 * A configuration for auditing
 */
@Serializable
data class AuditingConfig(
    /**
     * Whether or not to audit member joins
     */
    val joins: Boolean = false,
    /**
     * Whether or not to audit member leaves
     */
    val leaves: Boolean = false,
    /**
     * Whether or not to purges
     */
    val purge: Boolean = false,
    /**
     * Whether or not to audit kicks
     */
    val kicks: Boolean = false,
    /**
     * Whether or not to audit bans
     */
    val bans: Boolean = false,
    /**
     * Whether or not to audit username changes
     */
    val names: Boolean = false,
    /**
     * The channel to send audits to
     */
    val channel: Long? = null
) : Config
