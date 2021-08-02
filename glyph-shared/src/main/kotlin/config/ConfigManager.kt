/*
 * ConfigManager.kt
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

package org.yttr.glyph.shared.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.yttr.glyph.shared.config.server.AuditingConfig
import org.yttr.glyph.shared.config.server.QuickviewConfig
import org.yttr.glyph.shared.config.server.SelectableRolesConfig
import org.yttr.glyph.shared.config.server.ServerConfig
import org.yttr.glyph.shared.config.server.ServerConfigsTable
import org.yttr.glyph.shared.config.server.ServerSelectableRolesTable
import org.yttr.glyph.shared.config.server.ServerWikiSourcesTable
import org.yttr.glyph.shared.config.server.StarboardConfig
import org.yttr.glyph.shared.config.server.WikiConfig
import java.net.URI

/**
 * Manages the loading/saving of configs
 */
class ConfigManager(configure: Config.() -> Unit) {
    /**
     * HOCON-like config for the database director
     */
    class Config {
        /**
         * A uri the describes how to connect to the main database
         */
        var databaseConnectionUri: String = "localhost"

        /**
         * The driver used when connecting to the database, usually Postgres
         */
        var driver: String = "org.postgresql.Driver"
    }

    private val config = Config().also(configure)
    private val db = Database.run {
        val dbUri = URI(config.databaseConnectionUri)
        val userInfo = dbUri.userInfo.split(":")
        val username = userInfo[0]
        val password = userInfo[1]
        val connectionUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"

        connect(
            connectionUrl, driver = config.driver,
            user = username, password = password
        )
    }

    private val configs = mutableMapOf<Long, ServerConfig>()

    /**
     * The default config for when the server does not have one
     */
    val defaultConfig: ServerConfig = ServerConfig()

    // Some shorthand for long table names
    private val sct = ServerConfigsTable
    private val swst = ServerWikiSourcesTable
    private val ssrt = ServerSelectableRolesTable

//    /**
//     * Creates the database tables if they don't already exist
//     */
//    private fun createTables() {
//        transaction(db) {
//            SchemaUtils.createMissingTablesAndColumns(
//                sct,
//                swst,
//                ssrt
//            )
//        }
//    }

    /**
     * Load all the configurations from the database
     */
    private fun loadConfig(guildId: Long): ServerConfig = transaction(db) {
        val wikiSources = swst.select {
            swst.serverId.eq(guildId)
        }.map {
            it[swst.destination]
        }

        val selectableRoles = ssrt.select {
            ssrt.serverId.eq(guildId)
        }.map {
            it[ssrt.roleId]
        }

        sct.select {
            sct.serverId.eq(guildId)
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
    fun reloadServerConfig(guildId: Long) {
        configs[guildId] = loadConfig(guildId)
    }

    /**
     * Delete a guild's configuration from the database
     *
     * @param guildId the guild whose configuration to delete
     */
    suspend fun deleteServerConfig(guildId: Long): Int = newSuspendedTransaction(db = db) {
        sct.deleteWhere {
            sct.serverId.eq(guildId)
        }
    }

    /**
     * Get a guild's custom configuration or return the default if none found
     *
     * @param guildId the guild who's configuration to get
     *
     * @return the configuration
     */
    fun getServerConfig(guildId: Long): ServerConfig {
        return configs.getOrPut(guildId) {
            loadConfig(guildId)
        }
    }

    /**
     * Sets a guild's custom configuration
     *
     * @param guildId the guild who's configuration should be updates
     * @param config the new server config to try to apply
     */
    suspend fun setServerConfig(guildId: Long, config: ServerConfig): Unit = newSuspendedTransaction(db = db) {
        val sct = ServerConfigsTable

        // Lazy man's upsert
        sct.insertIgnore {
            it[sct.serverId] = guildId
        }

        sct.update({ sct.serverId.eq(guildId) }) {
            it[sct.serverId] = guildId
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
            swst.serverId.eq(guildId)
        }
        swst.batchInsert(config.wiki.sources, true) { wiki ->
            this[swst.serverId] = guildId
            this[swst.destination] = wiki
        }

        // Add in all the roles
        ssrt.deleteWhere {
            ssrt.serverId.eq(guildId)
        }
        ssrt.batchInsert(config.selectableRoles.roles, true) { role ->
            this[ssrt.serverId] = guildId
            this[ssrt.roleId] = role
        }

        configs[guildId] = config
    }
}
