package org.yttr.glyph.config.server

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Database table to store server configured wiki sources
 */
@Suppress("KDocMissingDocumentation")
object ServerWikiSourcesTable : Table() {
    val serverId = reference("ServerID", ServerConfigsTable.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val destination: Column<String> = varchar("Destination", 100)
}
