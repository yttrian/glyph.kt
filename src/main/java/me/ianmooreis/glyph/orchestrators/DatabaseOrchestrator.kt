package me.ianmooreis.glyph.orchestrators

import me.ianmooreis.glyph.configs.AuditingConfig
import me.ianmooreis.glyph.configs.QuickviewConfig
import me.ianmooreis.glyph.configs.SelectableRolesConfig
import me.ianmooreis.glyph.configs.ServerConfig
import me.ianmooreis.glyph.configs.StarboardConfig
import me.ianmooreis.glyph.configs.WikiConfig
import net.dv8tion.jda.core.entities.Guild
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Manages the configuration database
 */
object DatabaseOrchestrator {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
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
                " starboard_enabled, starboard_webhook, starboard_emoji, starboard_threshold, starboard_allow_self_starring)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                " ON CONFLICT (guild_id) DO UPDATE SET" +
                " (wiki_sources, wiki_min_quality, selectable_roles, selectable_roles_limit, " +
                " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                " auditing_webhook, auditing_joins, auditing_leaves, auditing_purge, auditing_kicks, auditing_bans, auditing_names, " +
                " starboard_enabled, starboard_webhook, starboard_emoji, starboard_threshold, starboard_allow_self_starring)" +
                " = (EXCLUDED.wiki_sources, EXCLUDED.wiki_min_quality, EXCLUDED.selectable_roles, EXCLUDED.selectable_roles_limit, " +
                " EXCLUDED.fa_quickview_enabled, " +
                " EXCLUDED.fa_quickview_thumbnail, EXCLUDED.picarto_quickview_enabled, " +
                " EXCLUDED.auditing_webhook, EXCLUDED.auditing_joins, EXCLUDED.auditing_leaves, EXCLUDED.auditing_purge, EXCLUDED.auditing_kicks, EXCLUDED.auditing_bans, EXCLUDED.auditing_names, " +
                " EXCLUDED.starboard_enabled, EXCLUDED.starboard_webhook, EXCLUDED.starboard_emoji, EXCLUDED.starboard_threshold, EXCLUDED.starboard_allow_self_starring)")
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
            ps.setBoolean(16, config.starboard.enabled)
            ps.setString(17, config.starboard.webhook)
            ps.setString(18, config.starboard.emoji)
            ps.setInt(19, config.starboard.threshold)
            ps.setBoolean(20, config.starboard.allowSelfStarring)
            ps.executeUpdate()
            con.close()
            configs.replace(guild.idLong, config)
            onSuccess()
        } catch (e: Exception) {
            log.warn(e.message)
            onFailure(e)
        }
    }
}

//TODO: Something better than this
/**
 * Attempts to convert an array from a PostgreSQL database into a list
 *
 * @param columnLabel the name of the column with the array
 *
 * @return hopefully a list from the array string
 */
fun ResultSet.getList(columnLabel: String): List<String> { //This is probably the stupidest thing in the history of stupid things, maybe ever.
    return this.getArray(columnLabel).toString().removeSurrounding("{", "}")
        .split(",").map { it.removeSurrounding("\"") }
}

/**
 * Attempts to add a list as a value in a prepared statement
 *
 * @param parameterIndex the index of the prepared parameter
 * @param list the list to set as a text array in the prepared statement
 */
fun PreparedStatement.setList(parameterIndex: Int, list: List<String?>) {
    this.setArray(parameterIndex, this.connection.createArrayOf("text", list.filterNotNull().filter { it != "" }.toTypedArray()))
}