/*
 * ConfigDirector.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors.config

import me.ianmooreis.glyph.directors.Director
import me.ianmooreis.glyph.directors.config.server.*
import me.ianmooreis.glyph.extensions.deleteConfig
import me.ianmooreis.glyph.extensions.upsert
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

/**
 * Manages the configuration database
 */
object ConfigDirector : Director() {
    private val configs = mutableMapOf<Long, ServerConfig>()
    private val dbUri = URI(System.getenv("DATABASE_URL"))
    private val username = dbUri.userInfo.split(":")[0]
    private val password = dbUri.userInfo.split(":")[1]
    private val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
    private val defaultConfig = ServerConfig()

    // Some shorthand for long table names
    private val sct = ServerConfigsTable
    private val swst = ServerWikiSourcesTable
    private val ssrt = ServerSelectableRolesTable

    init {
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = username, password = password)

        createTables()
        loadAllConfigs()
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
    fun loadAllConfigs() {
        configs.clear()

        transaction {
            sct.selectAll().forEach { r ->
                val serverId = r[sct.serverId]

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

                configs[serverId] = ServerConfig(
                    WikiConfig(wikiSources, r[sct.wikiMinQuality]),
                    SelectableRolesConfig(selectableRoles, r[sct.selectableRolesLimit]),
                    QuickviewConfig(r[sct.quickviewFuraffinityEnabled], r[sct.quickviewFuraffinityThumbnail], r[sct.quickviewPicartoEnabled]),
                    AuditingConfig(r[sct.logJoins], r[sct.logLeaves], r[sct.logPurge], r[sct.logKicks], r[sct.logBans], r[sct.logNames], r[sct.logChannel]),
                    AutoModConfig(r[sct.autoModBanUrlNames]),
                    StarboardConfig(r[sct.starboardEnabled], r[sct.starboardChannel], r[sct.starboardEmoji], r[sct.starboardThreshold], r[sct.starboardAllowSelfStar])
                )
            }
        }
    }

    /**
     * Delete a guild's configuration from the database
     *
     * @param guild the guild who's configuration to delete
     */
    fun deleteServerConfig(guild: Guild) {
        transaction {
            sct.deleteIgnoreWhere {
                sct.serverId.eq(guild.idLong)
            }
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
        return configs.getOrDefault(guild.idLong, defaultConfig)
    }

    /**
     * Checks is a guild has a custom configuration
     *
     * @param guild the guild to check for a custom config
     */
    fun hasCustomConfig(guild: Guild): Boolean {
        return configs.containsKey(guild.idLong)
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
     * @param guild     the guild who's configuration should be updates
     * @param config    the new server config to try to apply
     * @param onSuccess the callback to run if the update is successful
     * @param onFailure the callback to run if the update fails
     */
    fun setServerConfig(guild: Guild, config: ServerConfig, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val sct = ServerConfigsTable
        val serverId = guild.idLong

        transaction {
            sct.upsert(sct.serverId) {
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
                it[sct.logChannel] = config.auditing.webhook
                it[sct.autoModBanUrlNames] = config.crucible.banURLsInNames
                it[sct.starboardEnabled] = config.starboard.enabled
                it[sct.starboardChannel] = config.starboard.webhook
                it[sct.starboardEmoji] = config.starboard.emoji
                it[sct.starboardThreshold] = config.starboard.threshold
                it[sct.starboardAllowSelfStar] = config.starboard.allowSelfStarring
            }.also {
                swst.deleteWhere {
                    swst.serverId.eq(serverId)
                }
                swst.batchInsert(config.wiki.sources, true) { wiki ->
                    this[swst.serverId] = serverId
                    this[swst.destination] = wiki
                }
                ssrt.deleteWhere {
                    ssrt.serverId.eq(serverId)
                }
                ssrt.batchInsert(config.selectableRoles.roles, true) { role ->
                    this[ssrt.serverId] = serverId
                    this[ssrt.roleId] = role
                }
            }
        }
    }

    /**
     * Delete a guild's config when the server if left
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.guild.deleteConfig()
    }
}

