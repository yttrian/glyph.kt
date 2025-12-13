package org.yttr.glyph.bot

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.lettuce.core.RedisClient
import net.dv8tion.jda.api.JDABuilder
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
        val jda = JDABuilder.createDefault(conf.getString("discord-token"))
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .build()

        val commands = jda.updateCommands()

        for (module in modules) {
            jda.addEventListener(module)
            module.register()
            commands.addCommands(module.commands())
        }

        commands.queue()
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main(): Unit = Glyph.run()
