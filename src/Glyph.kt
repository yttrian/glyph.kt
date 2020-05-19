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

package me.ianmooreis.glyph

import me.ianmooreis.glyph.ai.AIAgent
import me.ianmooreis.glyph.ai.dialogflow.Dialogflow
import me.ianmooreis.glyph.database.DatabaseDirector
import me.ianmooreis.glyph.directors.AuditingDirector
import me.ianmooreis.glyph.directors.StarboardDirector
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.servers.BotList
import me.ianmooreis.glyph.directors.servers.ServerDirector
import me.ianmooreis.glyph.directors.skills.SkillDirector
import me.ianmooreis.glyph.messaging.MessagingDirector
import me.ianmooreis.glyph.messaging.quickview.QuickviewDirector
import me.ianmooreis.glyph.skills.DoomsdayClockSkill
import me.ianmooreis.glyph.skills.EphemeralSaySkill
import me.ianmooreis.glyph.skills.FallbackSkill
import me.ianmooreis.glyph.skills.FeedbackSkill
import me.ianmooreis.glyph.skills.HelpSkill
import me.ianmooreis.glyph.skills.RankSkill
import me.ianmooreis.glyph.skills.RedditSkill
import me.ianmooreis.glyph.skills.SnowstampSkill
import me.ianmooreis.glyph.skills.SourceSkill
import me.ianmooreis.glyph.skills.StatusSkill
import me.ianmooreis.glyph.skills.TimeSkill
import me.ianmooreis.glyph.skills.configuration.ServerConfigSkill
import me.ianmooreis.glyph.skills.creator.ChangeStatusSkill
import me.ianmooreis.glyph.skills.moderation.BanSkill
import me.ianmooreis.glyph.skills.moderation.GuildInfoSkill
import me.ianmooreis.glyph.skills.moderation.KickSkill
import me.ianmooreis.glyph.skills.moderation.PurgeSkill
import me.ianmooreis.glyph.skills.moderation.UserInfoSkill
import me.ianmooreis.glyph.skills.roles.RoleListSkill
import me.ianmooreis.glyph.skills.roles.RoleSetSkill
import me.ianmooreis.glyph.skills.roles.RoleUnsetSkill
import me.ianmooreis.glyph.skills.wiki.WikiSkill
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import redis.clients.jedis.JedisPool

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
        databaseConnectionUrl = System.getenv("DATABASE_URL")
        redisConnectionUrl = System.getenv("REDIS_URL")
    }

    private val redisPool: JedisPool = databaseDirector.redisPool

    /**
     * Build the bot and run
     */
    fun run() {
        SkillDirector.addSkill(
            HelpSkill(),
            StatusSkill(redisPool),
            SourceSkill(),
            RoleSetSkill,
            RoleUnsetSkill,
            RoleListSkill,
            ServerConfigSkill,
            PurgeSkill,
            UserInfoSkill,
            GuildInfoSkill,
            KickSkill(),
            BanSkill(),
            RankSkill(),
            EphemeralSaySkill(),
            RedditSkill(),
            WikiSkill,
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

            it.addEventListeners(
                MessagingDirector(aiAgent, redisPool), AuditingDirector,
                serverDirector, QuickviewDirector, StatusDirector, StarboardDirector
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