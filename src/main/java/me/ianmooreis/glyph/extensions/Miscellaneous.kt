package me.ianmooreis.glyph.extensions

import java.time.OffsetDateTime
import java.util.*

fun OffsetDateTime.toDate(): Date = Date.from(this.toInstant())

fun <T> List<T>.random(): T? {
    return if (this.isNotEmpty()) this[Random().nextInt(this.size)] else null
}