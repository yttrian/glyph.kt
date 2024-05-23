package org.yttr.glyph.compliance

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.SchemaUtils.withDataBaseLock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An interface for managing compliance
 */
object ComplianceOfficer {
    private val initialized = AtomicBoolean(false)

    private suspend fun Transaction.ensureTable() {
        if (initialized.compareAndSet(false, true)) {
            suspendedTransaction {
                withDataBaseLock {
                    createMissingTablesAndColumns(ComplianceTable)
                }
            }
        }
    }

    private fun where(userId: Long, complianceCategory: ComplianceCategory): Op<Boolean> {
        return (ComplianceTable.userId eq userId) and (ComplianceTable.complianceCategory eq complianceCategory)
    }

    /**
     * Check the opt-in status for a [userId] in a [complianceCategory].
     */
    suspend fun check(userId: Long, complianceCategory: ComplianceCategory): Boolean = newSuspendedTransaction {
        ensureTable()

        ComplianceTable.select(where(userId, complianceCategory)).firstOrNull()?.get(ComplianceTable.optedIn)
            ?: complianceCategory.optInDefault
    }

    /**
     * Record the decision of a [userId] in a [complianceCategory].
     */
    suspend fun decide(userId: Long, complianceCategory: ComplianceCategory, optedIn: Boolean) {
        newSuspendedTransaction {
            ensureTable()

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
