/*
 * Glyph.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.bot

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.yttr.glyph.bot.ai.AIAgent
import org.yttr.glyph.bot.ai.dialogflow.Dialogflow
import org.yttr.glyph.bot.directors.AuditingDirector
import org.yttr.glyph.bot.directors.StatusDirector
import org.yttr.glyph.bot.directors.config.ConfigDirector
import org.yttr.glyph.bot.directors.servers.BotList
import org.yttr.glyph.bot.directors.servers.ServerDirector
import org.yttr.glyph.bot.directors.starboard.StarboardDirector
import org.yttr.glyph.bot.messaging.MessagingDirector
import org.yttr.glyph.bot.messaging.quickview.QuickviewDirector
import org.yttr.glyph.bot.skills.FallbackSkill
import org.yttr.glyph.bot.skills.SkillDirector
import org.yttr.glyph.bot.skills.creator.ChangeStatusSkill
import org.yttr.glyph.bot.skills.moderation.BanSkill
import org.yttr.glyph.bot.skills.moderation.GuildInfoSkill
import org.yttr.glyph.bot.skills.moderation.KickSkill
import org.yttr.glyph.bot.skills.moderation.PurgeSkill
import org.yttr.glyph.bot.skills.moderation.UserInfoSkill
import org.yttr.glyph.bot.skills.play.DoomsdayClockSkill
import org.yttr.glyph.bot.skills.play.EphemeralSaySkill
import org.yttr.glyph.bot.skills.play.RankSkill
import org.yttr.glyph.bot.skills.play.RedditSkill
import org.yttr.glyph.bot.skills.roles.RoleListSkill
import org.yttr.glyph.bot.skills.roles.RoleSetSkill
import org.yttr.glyph.bot.skills.roles.RoleUnsetSkill
import org.yttr.glyph.bot.skills.util.FeedbackSkill
import org.yttr.glyph.bot.skills.util.HelpSkill
import org.yttr.glyph.bot.skills.util.ServerConfigSkill
import org.yttr.glyph.bot.skills.util.SnowstampSkill
import org.yttr.glyph.bot.skills.util.SourceSkill
import org.yttr.glyph.bot.skills.util.StatusSkill
import org.yttr.glyph.bot.skills.util.TimeSkill
import org.yttr.glyph.bot.skills.wiki.WikiSkill
import org.yttr.glyph.shared.pubsub.redis.RedisAsync

/**
 * The Glyph object to use when building the client
 */
object Glyph {
    /**
     * The current version of Glyph
     */
    val version: String = System.getenv("HEROKU_RELEASE_VERSION") ?: "?"

    private val aiAgent: AIAgent = Dialogflow(System.getenv("DIALOGFLOW_CREDENTIALS").byteInputStream())

    private val redis: RedisAsync = RedisClient.create().run {
        val redisUri = RedisURI.create(System.getenv("REDIS_URL")).apply {
            // We are using Heroku Redis which is version 5, but for some reason they give us a username.
            // However if we supply the username it runs the version 6 command and fails to login.
            username = null
        }
        connect(redisUri).async()
    }

    private val configDirector = ConfigDirector {
        databaseConnectionUri = System.getenv("DATABASE_URL")
    }

    private val skillDirector = SkillDirector().addSkill(
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

    /**
     * Build the bot and run
     */
    fun run() {
        val builder = DefaultShardManagerBuilder.createLight(null).also {
            val token = System.getenv("DISCORD_TOKEN")

            it.setToken(token)

            it.setEnabledIntents(
                GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS
            )

            it.enableCache(CacheFlag.EMOTE)

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

            val messagingDirector = MessagingDirector(aiAgent, redis, skillDirector)

            fun addDirectors(vararg directors: Director) {
                directors.forEach { director ->
                    director.configDirector = configDirector
                    it.addEventListeners(director)
                }
            }

            addDirectors(
                messagingDirector, AuditingDirector, skillDirector, configDirector,
                serverDirector, QuickviewDirector(messagingDirector), StatusDirector, StarboardDirector(redis)
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
