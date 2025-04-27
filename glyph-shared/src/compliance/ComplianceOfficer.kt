package org.yttr.glyph.shared.compliance

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

/**
 * An interface for managing compliance
 */
object ComplianceOfficer {
    private fun where(userId: Long, complianceCategory: ComplianceCategory): Op<Boolean> {
        return (ComplianceTable.userId eq userId) and (ComplianceTable.complianceCategory eq complianceCategory)
    }

    /**
     * Check the opt-in status for a [userId] in a [complianceCategory].
     */
    suspend fun check(userId: Long, complianceCategory: ComplianceCategory): Boolean = newSuspendedTransaction {
        ComplianceTable.select(ComplianceTable.optedIn)
            .where(where(userId, complianceCategory))
            .firstOrNull()
            ?.get(ComplianceTable.optedIn) ?: complianceCategory.optInDefault
    }

    /**
     * Record the decision of a [userId] in a [complianceCategory].
     */
    suspend fun decide(userId: Long, complianceCategory: ComplianceCategory, optedIn: Boolean) {
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
}
