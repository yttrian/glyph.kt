package org.yttr.glyph

import dev.kord.core.Kord
import org.koin.core.component.KoinComponent

interface Director : KoinComponent {
    fun register(kord: Kord)
}

/**
 * Register a director with Kord
 */
fun Kord.director(vararg director: Director) = director.forEach { it.register(this) }
