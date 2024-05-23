package org.yttr.glyph.shared.config.server

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Database table to store server configured selectable roles
 */
@Suppress("KDocMissingDocumentation")
object ServerSelectableRolesTable : Table() {
    val serverId: Column<Long> = long("ServerID")
        .primaryKey(0)
        .references(ServerConfigsTable.serverId, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val roleId: Column<Long> = long("RoleID").primaryKey(1)
}
