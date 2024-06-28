package org.yttr.glyph

import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import org.yttr.glyph.quickviews.QuickViews
import org.yttr.glyph.skills.Skills

/**
 * Where everything begins.
 */
@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val conf = ConfigFactory.load()
    val kord = Kord(conf.getString("discord.token"))

    kord.on<ReadyEvent>(consumer = Skills::consume)
    kord.on<MessageCreateEvent>(consumer = Skills::consume)
    kord.on<MessageCreateEvent>(consumer = QuickViews::consume)

    kord.login {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }
}
