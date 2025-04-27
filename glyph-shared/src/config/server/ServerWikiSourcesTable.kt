package org.yttr.glyph.shared.config.server

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Database table to store server configured wiki sources
 */
@Suppress("KDocMissingDocumentation")
object ServerWikiSourcesTable : Table() {
    val serverId: Column<Long> = long("ServerID")
        .references(
            ref = ServerConfigsTable.serverId,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.CASCADE
        )
    val destination: Column<String> = varchar("Destination", length = 100)

    init {
        uniqueIndex(serverId, destination)
    }
}
