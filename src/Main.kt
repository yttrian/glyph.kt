package org.yttr.glyph

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.lettuce.core.RedisClient
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.yttr.glyph.data.RedisAsync
import org.yttr.glyph.presentation.ServerDirector
import org.yttr.glyph.presentation.StatusDirector
import org.yttr.glyph.quickviews.QuickViewDirector
import org.yttr.glyph.skills.SkillDirector
import org.yttr.glyph.starboard.StarboardDirector

/**
 * Where everything begins.
 */
@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val koin = startKoin {
        modules(glyphModule)
    }.koin

    val kord = Kord(koin.get<Config>().getString("discord.token"))

    kord.director(QuickViewDirector)
    kord.director(ServerDirector)
    kord.director(SkillDirector)
    kord.director(StarboardDirector)
    kord.director(StatusDirector)

    kord.login {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }
}

private val glyphModule = module {
    single<Config> { ConfigFactory.load() }

    single<RedisAsync> {
        RedisClient.create(get<Config>().getString("data.redis-url")).connect().async()
    }
}
