package org.yttr.glyph.bot.data

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object StarboardTable : LongIdTable("starboards"), Timestamped {
    val guildId = long("guild_id").uniqueIndex()
    val enabled = bool("enabled")
    val channelId = long("channel_id")
    val emoji = varchar("emoji", length = 32)
    val threshold = integer("threshold")
    val canSelfStar = bool("can_self_star")

    override val createdAt: Column<Instant?> = timestamp("created_at").nullable()
    override val updatedAt: Column<Instant?> = timestamp("updated_at").nullable()
}

object QuickViewTable : LongIdTable("quickviews"), Timestamped {
    val guildId = long("guild_id").uniqueIndex()
    val furAffinity = bool("fur_affinity_enabled")

    override val createdAt: Column<Instant?> = timestamp("created_at").nullable()
    override val updatedAt: Column<Instant?> = timestamp("updated_at").nullable()
}

interface Timestamped {
    val createdAt: Column<Instant?>
    val updatedAt: Column<Instant?>
}
