package org.yttr.glyph.skills

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import org.yttr.glyph.Director

/**
 * The skill director calls upon available skills based on NLP or slash-command results.
 */
object SkillDirector : Director {
    override fun register(kord: Kord) {
        kord.on<MessageCreateEvent>(consumer = ::process)
    }

    private suspend fun process(event: MessageCreateEvent) {
        if (event.message.mentionedUserIds.contains(event.kord.selfId)) {
            return
        }
    }
}
