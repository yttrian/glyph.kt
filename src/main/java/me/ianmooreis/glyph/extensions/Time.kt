package me.ianmooreis.glyph.extensions

import java.time.OffsetDateTime
import java.util.*

fun OffsetDateTime.toDate(): Date = Date.from(this.toInstant())