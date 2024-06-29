package org.yttr.glyph.presentation

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.yttr.glyph.Director
import kotlin.time.Duration.Companion.hours

/**
 * Manages the status messages of the client
 */
object StatusDirector : Director {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun register(kord: Kord) {
        kord.on<ReadyEvent> {
            log.info("Ready on shard $shard with ${guilds.count()} guilds")

            while (true) {
                TODO("Random Status")
                delay(1.hours)
            }
        }
    }
}
