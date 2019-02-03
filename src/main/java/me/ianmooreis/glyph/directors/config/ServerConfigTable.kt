/*
 * ServerConfigTable.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2019 by Ian Moore
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

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.or

object ServerConfigTable : Table() {
    val serverId = long("ServerID").primaryKey()
    val autoModBanUrlNames = bool("AutoModBanUrlNames").default(false)
    val logChannel = varchar("LogChannel", 32).nullable()
    val logJoins = bool("LogJoin").default(false)
    val logLeaves = bool("LogLeave").default(false)
    val logKicks = bool("LogKick").default(false)
    val logBans = bool("LogBan").default(false)
    val logNames = bool("LogNameChange").default(false)
    val logPurge = bool("LogPurge").default(false)
    val selectableRolesLimit = integer("SelectableRolesLimit").default(1).check { it.eq(-1).or(it.greaterEq(1)) }
    val starboardEnabled = bool("StarboardEnabled").default(false)
    val starboardChannel = varchar("StarboardChannel", 32).nullable()
    val starboardEmoji = varchar("StarboardEmoji", 32).default("star")
    val starboardThreshold = integer("StarboardThreshold").default(1).check { it.greaterEq(1) }
    val starboardAllowSelfStar = bool("StarboardAllowSelfStar").default(false)
    val quickviewFuraffinityEnabled = bool("QuickviewFuraffinityEnabled").default(true)
    val quickviewFuraffinityThumbnail = bool("QuickviewFuraffinityThumbnail").default(true)
    val quickviewPicartoEnabled = bool("QuickviewPicartoEnabled").default(true)
    val wikiMinQuality = integer("WikiMinQuality").default(50).check { it.between(0, 100) }
}