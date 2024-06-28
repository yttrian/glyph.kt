package org.yttr.glyph

import dev.kord.core.event.Event

interface Consumer<T : Event> {
    suspend fun consume(event: T)
}
