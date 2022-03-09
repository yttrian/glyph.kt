/*
 * ComplianceOfficer.kt
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

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

/**
 * An interface for managing compliance
 */
object ComplianceOfficer {
    init {
        // FIXME: Blocking table creation
        transaction {
            SchemaUtils.createMissingTablesAndColumns(ComplianceTable)
        }
    }

    private fun where(userId: Long, complianceCategory: ComplianceCategory): Op<Boolean> {
        return (ComplianceTable.userId eq userId) and (ComplianceTable.complianceCategory eq complianceCategory)
    }

    /**
     * Check the opt-in status for a [userId] in a [complianceCategory]
     */
    suspend fun check(userId: Long, complianceCategory: ComplianceCategory): Boolean = newSuspendedTransaction {
        ComplianceTable.select(where(userId, complianceCategory)).firstOrNull()?.get(ComplianceTable.optedIn)
            ?: complianceCategory.optInDefault
    }

    /**
     * Record the decision of a [userId] in a [complianceCategory]
     */
    suspend fun decide(userId: Long, complianceCategory: ComplianceCategory, optedIn: Boolean) =
        newSuspendedTransaction {
            fun ComplianceTable.save(ub: UpdateBuilder<*>) {
                ub[this.userId] = userId
                ub[this.complianceCategory] = complianceCategory
                ub[this.optedIn] = optedIn
                ub[this.decided] = Instant.now()
            }

            ComplianceTable.insertIgnore {
                save(it)
            }

            ComplianceTable.update({ where(userId, complianceCategory) }) {
                save(it)
            }
        }
}
