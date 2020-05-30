/*
 * Glyph.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ianmooreis.glyph.bot

import me.ianmooreis.glyph.bot.ai.AIAgent
import me.ianmooreis.glyph.bot.ai.dialogflow.Dialogflow
import me.ianmooreis.glyph.bot.database.DatabaseDirector
import me.ianmooreis.glyph.bot.database.RedisAsync
import me.ianmooreis.glyph.bot.database.config.ConfigDirector
import me.ianmooreis.glyph.bot.directors.AuditingDirector
import me.ianmooreis.glyph.bot.directors.StarboardDirector
import me.ianmooreis.glyph.bot.directors.StatusDirector
import me.ianmooreis.glyph.bot.directors.servers.BotList
import me.ianmooreis.glyph.bot.directors.servers.ServerDirector
import me.ianmooreis.glyph.bot.directors.skills.SkillDirector
import me.ianmooreis.glyph.bot.messaging.MessagingDirector
import me.ianmooreis.glyph.bot.messaging.quickview.QuickviewDirector
import me.ianmooreis.glyph.bot.skills.DoomsdayClockSkill
import me.ianmooreis.glyph.bot.skills.EphemeralSaySkill
import me.ianmooreis.glyph.bot.skills.FallbackSkill
import me.ianmooreis.glyph.bot.skills.FeedbackSkill
import me.ianmooreis.glyph.bot.skills.HelpSkill
import me.ianmooreis.glyph.bot.skills.RankSkill
import me.ianmooreis.glyph.bot.skills.RedditSkill
import me.ianmooreis.glyph.bot.skills.SnowstampSkill
import me.ianmooreis.glyph.bot.skills.SourceSkill
import me.ianmooreis.glyph.bot.skills.StatusSkill
import me.ianmooreis.glyph.bot.skills.TimeSkill
import me.ianmooreis.glyph.bot.skills.configuration.ServerConfigSkill
import me.ianmooreis.glyph.bot.skills.creator.ChangeStatusSkill
import me.ianmooreis.glyph.bot.skills.moderation.BanSkill
import me.ianmooreis.glyph.bot.skills.moderation.GuildInfoSkill
import me.ianmooreis.glyph.bot.skills.moderation.KickSkill
import me.ianmooreis.glyph.bot.skills.moderation.PurgeSkill
import me.ianmooreis.glyph.bot.skills.moderation.UserInfoSkill
import me.ianmooreis.glyph.bot.skills.roles.RoleListSkill
import me.ianmooreis.glyph.bot.skills.roles.RoleSetSkill
import me.ianmooreis.glyph.bot.skills.roles.RoleUnsetSkill
import me.ianmooreis.glyph.bot.skills.wiki.WikiSkill
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder

/**
 * The Glyph object to use when building the client
 */
object Glyph {
    /**
     * The current version of Glyph
     */
    val version: String = System.getenv("HEROKU_RELEASE_VERSION") ?: "?"

    private val aiAgent: AIAgent = Dialogflow(System.getenv("DIALOGFLOW_CREDENTIALS").byteInputStream())

    private val databaseDirector = DatabaseDirector {
        databaseConnectionUri = System.getenv("DATABASE_URL")
        redisConnectionUri = System.getenv("REDIS_URL")
    }

    private val redis: RedisAsync = databaseDirector.redis

    /**
     * Build the bot and run
     */
    fun run() {
        SkillDirector.addSkill(
            HelpSkill(),
            StatusSkill(redis),
            SourceSkill(),
            RoleSetSkill(),
            RoleUnsetSkill(),
            RoleListSkill(),
            ServerConfigSkill(),
            PurgeSkill(),
            UserInfoSkill(),
            GuildInfoSkill(),
            KickSkill(),
            BanSkill(),
            RankSkill(),
            EphemeralSaySkill(),
            RedditSkill(),
            WikiSkill(),
            TimeSkill(),
            FeedbackSkill(),
            DoomsdayClockSkill(),
            SnowstampSkill(),
            ChangeStatusSkill(),
            FallbackSkill()
        )

        val builder = DefaultShardManagerBuilder.createLight(null).also {
            val token = System.getenv("DISCORD_TOKEN")

            it.setToken(token)

            it.setEnabledIntents(
                GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS
            )

            val serverDirector = ServerDirector { id ->
                val discordBotList = BotList(
                    "Discord Bot List",
                    "https://top.gg/api/bots/$id/stats",
                    System.getenv("DISCORDBOTLIST_TOKEN")
                )
                val discordBots = BotList(
                    "Discord Bots",
                    "https://bots.discord.pw/api/bots/$id/stats",
                    System.getenv("DISCORDBOTS_TOKEN")
                )

                botList(discordBotList, discordBots)
            }

            val messagingDirector = MessagingDirector(aiAgent, redis)

            it.addEventListeners(
                messagingDirector, AuditingDirector, ConfigDirector,
                serverDirector, QuickviewDirector(messagingDirector), StatusDirector, StarboardDirector
            )
        }

        builder.build()
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main(): Unit = Glyph.run()
