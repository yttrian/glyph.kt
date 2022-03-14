/*
 * ComplianceTable.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2022 by Ian Moore
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

package org.yttr.glyph.shared.compliance

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

/**
 * Compliance tracking table
 */
object ComplianceTable : IntIdTable("Compliance", "ComplianceID") {
    /**
     * The user this decision belongs to.
     */
    val userId: Column<Long> = long("UserID")

    /**
     * The [ComplianceCategory] being tracked.
     */
    val complianceCategory: Column<ComplianceCategory> = enumeration("Category", ComplianceCategory::class)

    /**
     * Did the user opt-in?
     */
    val optedIn: Column<Boolean> = bool("OptedIn")

    /**
     * [Instant] when the latest decision was made.
     */
    val decided: Column<Instant> = timestamp("DecidedAt").defaultExpression(CurrentTimestamp())

    init {
        uniqueIndex(userId, complianceCategory)
    }
}
