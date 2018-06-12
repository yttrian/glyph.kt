package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.entities.Guild
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

data class ServerConfig(val wiki: WikiConfig = WikiConfig(), val selectableRoles: SelectableRolesConfig = SelectableRolesConfig(),
                        val quickview: QuickviewConfig = QuickviewConfig(), val auditing: AuditingConfig = AuditingConfig(),
                        val starboard: StarboardConfig = StarboardConfig())
data class WikiConfig(val sources: List<String?> = listOf("wikipedia", "masseffect", "avp"), val minimumQuality: Int = 50)
data class SelectableRolesConfig(val roles: List<String?> = emptyList(), val limit: Int = 1)
data class AuditingConfig(val joins: Boolean = false, val leaves: Boolean = false, val purge: Boolean = false, val kicks: Boolean = false, val bans: Boolean = false, val names: Boolean = false, val webhook: String? = null)
data class QuickviewConfig(val furaffinityEnabled: Boolean = true, val furaffinityThumbnails: Boolean = false, val picartoEnabled: Boolean = true)
data class StarboardConfig(val enabled: Boolean = false, val webhook: String? = null, val emoji: String = "star", val threshold: Int = 1, val allowSelfStarring: Boolean = false)

object DatabaseOrchestrator {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val configs = mutableMapOf<Long, ServerConfig>()
    private val dbUri = URI(System.getenv("DATABASE_URL"))
    private val username = dbUri.userInfo.split(":")[0]
    private val password = dbUri.userInfo.split(":")[1]
    private val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
    private val defaultConfig = ServerConfig()

    init {
        loadConfigs()
    }

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

    fun getServerConfig(guild: Guild): ServerConfig {
        return configs.getOrDefault(guild.idLong, defaultConfig)
    }

    fun hasCustomConfig(guild: Guild): Boolean {
        return configs.containsKey(guild.idLong)
    }

    fun getDefaultServerConfig(): ServerConfig {
        return defaultConfig
    }

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
fun ResultSet.getList(columnLabel: String): List<String> { //This is probably the stupidest thing in the history of stupid things, maybe ever.
    return this.getArray(columnLabel).toString().removeSurrounding("{","}")
            .split(",").map { it.removeSurrounding("\"") }
}

fun PreparedStatement.setList(parameterIndex: Int, list: List<String?>) {
    this.setArray(parameterIndex, this.connection.createArrayOf("text", list.filterNotNull().filter { it != "" }.toTypedArray()))
}