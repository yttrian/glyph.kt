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
import me.ianmooreis.glyph.extensions.deleteConfig
import me.ianmooreis.glyph.extensions.getList
import me.ianmooreis.glyph.extensions.setList
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import org.postgresql.util.PSQLException
import java.net.URI
import java.sql.DriverManager

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

    init {
        loadConfigs()
    }

    /**
     * Load all the configurations from the database
     */
    fun loadConfigs() {
        val con = DriverManager.getConnection(dbUrl, username, password)
        val ps = con.prepareStatement("SELECT * FROM serverconfigs") //TODO: Not select *
        val rs = ps.executeQuery()
        configs.clear()
        while (rs.next()) {
            configs[rs.getLong("guild_id")] = ServerConfig(
                WikiConfig(
                    rs.getList("wiki_sources"),
                    rs.getInt("wiki_min_quality")
                ),
                SelectableRolesConfig(
                    rs.getList("selectable_roles"),
                    rs.getInt("selectable_roles_limit")),
                QuickviewConfig(
                    rs.getBoolean("fa_quickview_enabled"),
                    rs.getBoolean("fa_quickview_thumbnail"),
                    rs.getBoolean("picarto_quickview_enabled")),
                AuditingConfig(
                    rs.getBoolean("auditing_joins"),
                    rs.getBoolean("auditing_leaves"),
                    rs.getBoolean("auditing_purge"),
                    rs.getBoolean("auditing_kicks"),
                    rs.getBoolean("auditing_bans"),
                    rs.getBoolean("auditing_names"),
                    rs.getString("auditing_webhook")),
                CrucibleConfig(
                    rs.getBoolean("crucible_ban_urls_in_names")
                ),
                StarboardConfig(
                    rs.getBoolean("starboard_enabled"),
                    rs.getString("starboard_webhook"),
                    rs.getString("starboard_emoji"),
                    rs.getInt("starboard_threshold"),
                    rs.getBoolean("starboard_allow_self_starring"))
            )
        }
        con.close()
    }

    /**
     * Delete a guild's configuration from the database
     *
     * @param guild the guild who's configuration to delete
     */
    fun deleteServerConfig(guild: Guild) {
        try {
            configs.remove(guild.idLong)
            val con = DriverManager.getConnection(dbUrl, username, password)
            val ps = con.prepareStatement("DELETE FROM serverconfigs WHERE guild_id = ?")
            ps.setLong(1, guild.idLong)
            ps.executeQuery()
            con.commit()
            con.close()
        } catch (e: PSQLException) {
            log.debug("Failed to delete config for $guild! (Maybe it never existed?)")
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
        try {
            val con = DriverManager.getConnection(dbUrl, username, password)
            val ps = con.prepareStatement("INSERT INTO serverconfigs" +
                " (guild_id, wiki_sources, wiki_min_quality, selectable_roles, selectable_roles_limit, " +
                " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                " auditing_webhook, auditing_joins, auditing_leaves, auditing_purge, auditing_kicks, auditing_bans, auditing_names, " +
                " crucible_ban_urls_in_names, starboard_enabled, starboard_webhook, starboard_emoji, starboard_threshold, starboard_allow_self_starring)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                " ON CONFLICT (guild_id) DO UPDATE SET" +
                " (wiki_sources, wiki_min_quality, selectable_roles, selectable_roles_limit, " +
                " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                " auditing_webhook, auditing_joins, auditing_leaves, auditing_purge, auditing_kicks, auditing_bans, auditing_names, " +
                " crucible_ban_urls_in_names, starboard_enabled, starboard_webhook, starboard_emoji, starboard_threshold, starboard_allow_self_starring)" +
                " = (EXCLUDED.wiki_sources, EXCLUDED.wiki_min_quality, EXCLUDED.selectable_roles, EXCLUDED.selectable_roles_limit, " +
                " EXCLUDED.fa_quickview_enabled, " +
                " EXCLUDED.fa_quickview_thumbnail, EXCLUDED.picarto_quickview_enabled, " +
                " EXCLUDED.auditing_webhook, EXCLUDED.auditing_joins, EXCLUDED.auditing_leaves, EXCLUDED.auditing_purge, EXCLUDED.auditing_kicks, EXCLUDED.auditing_bans, EXCLUDED.auditing_names, " +
                " EXCLUDED.crucible_ban_urls_in_names, EXCLUDED.starboard_enabled, EXCLUDED.starboard_webhook, EXCLUDED.starboard_emoji, EXCLUDED.starboard_threshold, EXCLUDED.starboard_allow_self_starring)")
            ps.setLong(1, guild.idLong)
            ps.setList(2, config.wiki.sources)
            ps.setInt(3, config.wiki.minimumQuality)
            ps.setList(4, config.selectableRoles.roles)
            ps.setInt(5, config.selectableRoles.limit)
            ps.setBoolean(6, config.quickview.furaffinityEnabled)
            ps.setBoolean(7, config.quickview.furaffinityThumbnails)
            ps.setBoolean(8, config.quickview.picartoEnabled)
            ps.setString(9, config.auditing.webhook)
            ps.setBoolean(10, config.auditing.joins)
            ps.setBoolean(11, config.auditing.leaves)
            ps.setBoolean(12, config.auditing.purge)
            ps.setBoolean(13, config.auditing.kicks)
            ps.setBoolean(14, config.auditing.bans)
            ps.setBoolean(15, config.auditing.names)
            ps.setBoolean(16, config.crucible.banURLsInNames)
            ps.setBoolean(17, config.starboard.enabled)
            ps.setString(18, config.starboard.webhook)
            ps.setString(19, config.starboard.emoji)
            ps.setInt(20, config.starboard.threshold)
            ps.setBoolean(21, config.starboard.allowSelfStarring)
            ps.executeUpdate()
            con.close()
            configs.replace(guild.idLong, config)
            onSuccess()
        } catch (e: Exception) {
            log.warn(e.message)
            onFailure(e)
        }
    }

    /**
     * Delete a guild's config when the server if left
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.guild.deleteConfig()
    }
}

