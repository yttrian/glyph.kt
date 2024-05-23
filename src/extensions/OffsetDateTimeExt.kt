package org.yttr.glyph.extensions

import java.time.OffsetDateTime
import java.util.Date

/**
 * Convert an OffsetDateTime to a Date
 */
fun OffsetDateTime.toDate(): Date = Date.from(this.toInstant())
