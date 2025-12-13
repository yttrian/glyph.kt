package org.yttr.glyph.bot

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.lettuce.core.RedisClient
import net.dv8tion.jda.api.requests.GatewayIntent
import org.yttr.glyph.bot.modules.HelpModule
import org.yttr.glyph.bot.modules.SnowstampModule
import org.yttr.glyph.bot.skills.config.ConfigDirector
import org.yttr.glyph.shared.pubsub.redis.RedisAsync


/**
 * The Glyph object to use when building the client
 */
object Glyph {
    /**
     * HOCON config from application.conf
     */
    val conf: Config = ConfigFactory.load().getConfig("glyph")

    /**
     * The current version of Glyph
     */
    val version: String = conf.getString("version").take(n = 7)

    private val redis: RedisAsync = RedisClient.create(conf.getString("data.redis-url")).connect().async()

    private val configDirector = ConfigDirector(
        jdbcDatabaseUrl = conf.getString("data.database-url"),
        redisUrl = conf.getString("data.redis-url")
    )

    val modules = listOf(
        HelpModule(),
        SnowstampModule()
    )

    /**
     * Build the bot and run
     */
    fun run() {
        val jda = default(conf.getString("discord-token")) {
            intents += GatewayIntent.GUILD_MESSAGES
        }

        for (module in modules) {
            module.boot(jda)
        }

        jda.updateCommands {
            for (module in modules) {
                module.updateCommands(commands = this)
            }
        }
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main(): Unit = Glyph.run()
