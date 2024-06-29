package org.yttr.glyph

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.ocpsoft.prettytime.PrettyTime

/**
 * Format with PrettyTime
 */
fun Instant.formatPrettyTime(): String = PrettyTime(this.toJavaInstant()).format(java.time.Instant.now())
