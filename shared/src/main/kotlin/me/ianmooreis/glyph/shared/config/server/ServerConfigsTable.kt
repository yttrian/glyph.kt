/*
 * ServerConfigsTable.kt
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

package me.ianmooreis.glyph.shared.config.server

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.or

/**
 * Database table to store server configs
 */
@Suppress("KDocMissingDocumentation")
object ServerConfigsTable : Table() {
    val serverId: Column<Long> = long("ServerID").primaryKey()
    val logChannelID: Column<Long?> = long("LogChannelID").nullable()
    val logJoins: Column<Boolean> = bool("LogJoin").default(false)
    val logLeaves: Column<Boolean> = bool("LogLeave").default(false)
    val logKicks: Column<Boolean> = bool("LogKick").default(false)
    val logBans: Column<Boolean> = bool("LogBan").default(false)
    val logNames: Column<Boolean> = bool("LogNameChange").default(false)
    val logPurge: Column<Boolean> = bool("LogPurge").default(false)
    val selectableRolesLimit: Column<Int> =
        integer("SelectableRolesLimit").default(1).check { it.eq(-1).or(it.greaterEq(1)) }
    val starboardEnabled: Column<Boolean> = bool("StarboardEnabled").default(false)
    val starboardChannelID: Column<Long?> = long("StarboardChannelID").nullable()
    val starboardEmoji: Column<String> = varchar("StarboardEmoji", 32).default("star")
    val starboardThreshold: Column<Int> = integer("StarboardThreshold").default(1).check { it.greaterEq(1) }
    val starboardAllowSelfStar: Column<Boolean> = bool("StarboardAllowSelfStar").default(false)
    val quickviewFuraffinityEnabled: Column<Boolean> = bool("QuickviewFuraffinityEnabled").default(true)
    val quickviewFuraffinityThumbnail: Column<Boolean> = bool("QuickviewFuraffinityThumbnail").default(true)
    val quickviewPicartoEnabled: Column<Boolean> = bool("QuickviewPicartoEnabled").default(true)
}
