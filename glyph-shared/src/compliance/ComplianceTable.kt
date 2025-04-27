package org.yttr.glyph.shared.compliance

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
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
    val decided: Column<Instant> = timestamp("DecidedAt").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(userId, complianceCategory)
    }
}
