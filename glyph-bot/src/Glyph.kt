package org.yttr.glyph.bot

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.lettuce.core.RedisClient
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.yttr.glyph.bot.ai.AIAgent
import org.yttr.glyph.bot.ai.dialogflow.Dialogflow
import org.yttr.glyph.bot.messaging.ComplianceListener
import org.yttr.glyph.bot.messaging.MessagingDirector
import org.yttr.glyph.bot.messaging.ThanksListener
import org.yttr.glyph.bot.messaging.quickview.QuickviewDirector
import org.yttr.glyph.bot.presentation.ServerDirector
import org.yttr.glyph.bot.presentation.StatusDirector
import org.yttr.glyph.bot.skills.SkillDirector
import org.yttr.glyph.bot.skills.config.ConfigDirector
import org.yttr.glyph.bot.skills.config.ServerConfigSkill
import org.yttr.glyph.bot.skills.creator.ChangeStatusSkill
import org.yttr.glyph.bot.skills.moderation.AuditingDirector
import org.yttr.glyph.bot.skills.moderation.BanSkill
import org.yttr.glyph.bot.skills.moderation.GuildInfoSkill
import org.yttr.glyph.bot.skills.moderation.KickSkill
import org.yttr.glyph.bot.skills.moderation.PurgeSkill
import org.yttr.glyph.bot.skills.moderation.UserInfoSkill
import org.yttr.glyph.bot.skills.play.DoomsdayClockSkill
import org.yttr.glyph.bot.skills.play.EphemeralSaySkill
import org.yttr.glyph.bot.skills.play.RankSkill
import org.yttr.glyph.bot.skills.roles.RoleListSkill
import org.yttr.glyph.bot.skills.roles.RoleSetSkill
import org.yttr.glyph.bot.skills.roles.RoleUnsetSkill
import org.yttr.glyph.bot.skills.starboard.StarboardDirector
import org.yttr.glyph.bot.skills.util.FallbackSkill
import org.yttr.glyph.bot.skills.util.FeedbackSkill
import org.yttr.glyph.bot.skills.util.HelpSkill
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
     * HOCON config from application.conf
     */
    val conf: Config = ConfigFactory.load().getConfig("glyph")

    /**
     * The current version of Glyph
     */
    val version: String = conf.getString("version").take(n = 7)

    private val aiAgent: AIAgent = Dialogflow(conf.getString("dialogflow.credentials").byteInputStream())

    private val redis: RedisAsync = RedisClient.create(conf.getString("data.redis-url")).connect().async()

    private val configDirector = ConfigDirector(
        jdbcDatabaseUrl = conf.getString("data.database-url"),
        redisUrl = conf.getString("data.redis-url")
    )

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
            val token = conf.getString("discord-token")

            it.setToken(token)

            it.setEnabledIntents(
                GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS
            )

            it.enableCache(CacheFlag.EMOTE)

            val serverDirector = ServerDirector()

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

            it.addEventListeners(ComplianceListener, ThanksListener)
        }

        builder.build()
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main(): Unit = Glyph.run()
