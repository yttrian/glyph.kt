package org.yttr.glyph

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.lettuce.core.RedisClient
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.yttr.glyph.data.Redis
import org.yttr.glyph.quickviews.QuickViews
import org.yttr.glyph.skills.Skills

/**
 * Where everything begins.
 */
@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val koin = startKoin {
        modules(glyphModule)
    }.koin

    val kord = Kord(koin.get<Config>().getString("discord.token"))

    kord.on<ReadyEvent>(consumer = Skills::consume)
    kord.on<MessageCreateEvent>(consumer = Skills::consume)
    kord.on<MessageCreateEvent>(consumer = QuickViews::consume)

    kord.login {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }
}

private val glyphModule = module {
    single<Config> { ConfigFactory.load() }

    single<Redis> {
        RedisClient.create(get<Config>().getString("data.redis-url")).connect().async()
    }
}
