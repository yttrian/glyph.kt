package org.yttr.glyph.config.server

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Database table to store server configured selectable roles
 */
@Suppress("KDocMissingDocumentation")
object ServerSelectableRolesTable : Table() {
    val serverId = reference("ServerID", ServerConfigsTable.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val roleId: Column<Long> = long("RoleID")
}
