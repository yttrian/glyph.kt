package org.yttr.glyph.bot.data

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DatabaseServerConfig(private val guildId: Long, private val database: Database) : ServerConfig() {
    override suspend fun getStarboardConfig(): Starboard {
        val row = newSuspendedTransaction(db = database) {
            StarboardTable.selectAll().where {
                StarboardTable.guildId eq guildId
            }.firstOrNull()
        }

        return if (row != null) {
            Starboard(
                enabled = row[StarboardTable.enabled],
                channel = row[StarboardTable.channelId],
                emoji = row[StarboardTable.emoji],
                threshold = row[StarboardTable.threshold],
                canSelfStar = row[StarboardTable.canSelfStar]
            )
        } else {
            Starboard()
        }
    }

    override suspend fun getQuickViewConfig(): QuickView {
        val row = newSuspendedTransaction(db = database) {
            QuickViewTable.selectAll().where {
                QuickViewTable.guildId eq guildId
            }.firstOrNull()
        }

        return if (row != null) {
            QuickView(furAffinityEnabled = row[QuickViewTable.furAffinity])
        } else {
            QuickView()
        }
    }
}
