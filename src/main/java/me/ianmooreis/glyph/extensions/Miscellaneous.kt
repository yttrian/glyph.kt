package me.ianmooreis.glyph.extensions

import java.time.OffsetDateTime
import java.util.Date
import java.util.Random

/**
 * Convert an OffsetDateTime to a Date
 */
fun OffsetDateTime.toDate(): Date = Date.from(this.toInstant())

/**
 * Grab a random item from a list or return null
 *
 * @return a random list item or null
 */
fun <T> List<T>.random(): T? {
    return if (this.isNotEmpty()) this[Random().nextInt(this.size)] else null
}