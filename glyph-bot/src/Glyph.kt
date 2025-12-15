package org.yttr.glyph.bot

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import net.dv8tion.jda.api.requests.GatewayIntent
import org.yttr.glyph.bot.data.DatabaseConfigStore
import org.yttr.glyph.bot.data.Redis
import org.yttr.glyph.bot.modules.ConfigModule
import org.yttr.glyph.bot.modules.HelpModule
import org.yttr.glyph.bot.modules.ObservatoryModule
import org.yttr.glyph.bot.modules.QuickViewModule
import org.yttr.glyph.bot.modules.SnowstampModule
import org.yttr.glyph.bot.modules.StarboardModule

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun main() {
    val conf: Config = ConfigFactory.load().getConfig("glyph")

    // Create JDA
    val jda = default(token = conf.getString("discord-token")) {
        intents += GatewayIntent.MESSAGE_CONTENT
    }

    // Connect to backing stores
    val configStore = DatabaseConfigStore.create(jdbcDatabaseUrl = conf.getString("data.database-url"))
    val redis = Redis.connect(uri = conf.getString("data.redis-url"))

    // Build all modules
    val modules = listOf(
        ConfigModule(),
        HelpModule(),
        SnowstampModule(),
        StarboardModule(redis, configStore),
        QuickViewModule(configStore),
        ObservatoryModule(webhookId = conf.getLong("management.logging-webhook"))
    )

    // Boot all modules
    for (module in modules) {
        module.boot(jda)
    }

    // Update commands
    jda.updateCommands {
        for (module in modules) {
            module.updateCommands(commands = this)
        }
    }.queue()
}
