package org.yttr.glyph.config.server

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.or

/**
 * Database table to store server configs
 */
@Suppress("KDocMissingDocumentation")
object ServerConfigsTable : LongIdTable(columnName = "ServerID") {
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
    val wikiMinQuality: Column<Int> = integer("WikiMinQuality").default(50).check { it.between(0, 100) }
}
