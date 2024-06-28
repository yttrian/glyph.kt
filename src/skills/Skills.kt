package org.yttr.glyph.skills

import dev.kord.core.behavior.reply
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.first

object Skills {
    suspend fun consume(event: ReadyEvent) {
        event.kord.createGlobalApplicationCommands {

        }
    }

    suspend fun consume(event: MessageCreateEvent) {
        if (event.message.mentionedUsers.first() != event.kord.getSelf()) {
            return
        }

        event.message.reply {
            content = "Hello world!"
        }
    }
}
