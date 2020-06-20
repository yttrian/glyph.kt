/*
 * ConfigDirector.kt
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

package me.ianmooreis.glyph.bot.database.config

import kotlinx.coroutines.launch
import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.bot.database.config.server.AuditingConfig
import me.ianmooreis.glyph.bot.database.config.server.QuickviewConfig
import me.ianmooreis.glyph.bot.database.config.server.SelectableRolesConfig
import me.ianmooreis.glyph.bot.database.config.server.ServerConfig
import me.ianmooreis.glyph.bot.database.config.server.ServerConfigsTable
import me.ianmooreis.glyph.bot.database.config.server.ServerSelectableRolesTable
import me.ianmooreis.glyph.bot.database.config.server.ServerWikiSourcesTable
import me.ianmooreis.glyph.bot.database.config.server.StarboardConfig
import me.ianmooreis.glyph.bot.database.config.server.WikiConfig
import me.ianmooreis.glyph.bot.extensions.deleteConfig
import me.ianmooreis.glyph.shared.pubsub.PubSubChannel
import me.ianmooreis.glyph.shared.pubsub.redis.RedisPubSub
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteIgnoreWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Manages the configuration database
 */
object ConfigDirector : Director() {
    private val configs = mutableMapOf<Long, ServerConfig>()
    private val defaultConfig = ServerConfig()

    /**
     * JDA instance for grabbing Guild data
     */
    lateinit var jda: JDA

    // Some shorthand for long table names
    private val sct = ServerConfigsTable
    private val swst = ServerWikiSourcesTable
    private val ssrt = ServerSelectableRolesTable

    private val redis = RedisPubSub {
        redisConnectionUri = System.getenv("REDIS_URL")
    }

    /**
     * Sets the JDA instance
     */
    override fun onReady(event: ReadyEvent) {
        jda = event.jda
    }

    init {
        createTables()

        redis.addResponder(PubSubChannel.CONFIG_PREFIX) { guildId ->
            val guild = jda.getGuildById(guildId)
            val guildConfig = guild?.let { getServerConfig(it) }
            guildConfig?.toJSON(guild) ?: ""
        }

        redis.addListener(PubSubChannel.CONFIG_REFRESH) { guildId ->
            jda.getGuildById(guildId)?.let { reloadServerConfig(it) }
        }
    }

    /**
     * Creates the database tables if they don't already exist
     */
    private fun createTables() {
        transaction {
            SchemaUtils.create(sct, swst, ssrt)
        }
    }

    /**
     * Load all the configurations from the database
     */
    private fun loadConfig(guild: Guild): ServerConfig = transaction {
        val serverId = guild.idLong

        val wikiSources = swst.select {
            swst.serverId.eq(serverId)
        }.map {
            it[swst.destination]
        }

        val selectableRoles = ssrt.select {
            ssrt.serverId.eq(serverId)
        }.map {
            it[ssrt.roleId]
        }

        sct.select {
            sct.serverId.eq(serverId)
        }.firstOrNull()?.let { r ->
            val quickviewConfig = QuickviewConfig(
                r[sct.quickviewFuraffinityEnabled],
                r[sct.quickviewFuraffinityThumbnail],
                r[sct.quickviewPicartoEnabled]
            )
            val auditingConfig = AuditingConfig(
                r[sct.logJoins],
                r[sct.logLeaves],
                r[sct.logPurge],
                r[sct.logKicks],
                r[sct.logBans],
                r[sct.logNames],
                r[sct.logChannelID]
            )
            val starboardConfig = StarboardConfig(
                r[sct.starboardEnabled],
                r[sct.starboardChannelID],
                r[sct.starboardEmoji],
                r[sct.starboardThreshold],
                r[sct.starboardAllowSelfStar]
            )
            val wikiConfig = WikiConfig(wikiSources, r[sct.wikiMinQuality])
            val selectableRolesConfig = SelectableRolesConfig(selectableRoles, r[sct.selectableRolesLimit])

            ServerConfig(wikiConfig, selectableRolesConfig, quickviewConfig, auditingConfig, starboardConfig)
        } ?: defaultConfig
    }

    /**
     * Force reloads a server config
     */
    fun reloadServerConfig(guild: Guild) {
        configs[guild.idLong] = loadConfig(guild)
    }

    /**
     * Delete a guild's configuration from the database
     *
     * @param guild the guild who's configuration to delete
     */
    suspend fun deleteServerConfig(guild: Guild): Int = newSuspendedTransaction {
        sct.deleteIgnoreWhere {
            sct.serverId.eq(guild.idLong)
        }
    }

    /**
     * Get a guild's custom configuration or return the default if none found
     *
     * @param guild the guild who's configuration to get
     *
     * @return the configuration
     */
    fun getServerConfig(guild: Guild): ServerConfig {
        return configs.getOrPut(guild.idLong) {
            loadConfig(guild)
        }
    }

    /**
     * Returns the default server configuration
     *
     * @return the default server configuration
     */
    fun getDefaultServerConfig(): ServerConfig {
        return defaultConfig
    }

    /**
     * Sets a guild's custom configuration
     *
     * @param guild the guild who's configuration should be updates
     * @param config the new server config to try to apply
     */
    suspend fun setServerConfig(guild: Guild, config: ServerConfig) = newSuspendedTransaction {
        val sct = ServerConfigsTable
        val serverId = guild.idLong

        // Lazy man's upsert
        sct.insertIgnore {
            it[sct.serverId] = serverId
        }

        sct.update({ sct.serverId.eq(serverId) }) {
            it[sct.serverId] = serverId
            it[sct.wikiMinQuality] = config.wiki.minimumQuality
            it[sct.selectableRolesLimit] = config.selectableRoles.limit
            it[sct.quickviewFuraffinityEnabled] = config.quickview.furaffinityEnabled
            it[sct.quickviewFuraffinityThumbnail] = config.quickview.furaffinityThumbnails
            it[sct.quickviewPicartoEnabled] = config.quickview.picartoEnabled
            it[sct.logJoins] = config.auditing.joins
            it[sct.logLeaves] = config.auditing.leaves
            it[sct.logPurge] = config.auditing.purge
            it[sct.logKicks] = config.auditing.kicks
            it[sct.logBans] = config.auditing.bans
            it[sct.logNames] = config.auditing.names
            it[sct.logChannelID] = config.auditing.channel
            it[sct.starboardEnabled] = config.starboard.enabled
            it[sct.starboardChannelID] = config.starboard.channel
            it[sct.starboardEmoji] = config.starboard.emoji
            it[sct.starboardThreshold] = config.starboard.threshold
            it[sct.starboardAllowSelfStar] = config.starboard.allowSelfStarring
        }

        // Add in all the wiki sources
        swst.deleteWhere {
            swst.serverId.eq(serverId)
        }
        swst.batchInsert(config.wiki.sources, true) { wiki ->
            this[swst.serverId] = serverId
            this[swst.destination] = wiki
        }

        // Add in all the roles
        ssrt.deleteWhere {
            ssrt.serverId.eq(serverId)
        }
        ssrt.batchInsert(config.selectableRoles.roles, true) { role ->
            this[ssrt.serverId] = serverId
            this[ssrt.roleId] = role
        }

        configs[serverId] = config
    }

    /**
     * Delete a guild's config when the server if left
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        launch { event.guild.deleteConfig() }
    }
}
