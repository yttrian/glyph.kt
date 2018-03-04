package me.ianmooreis.glyph.orchestrators

import com.google.gson.GsonBuilder
import net.dv8tion.jda.core.entities.Guild
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

data class ServerConfig(val wiki: String = "wikipedia", val selectableRoles: List<String?> = emptyList(),
                        val quickview: QuickviewConfig, val auditing: AuditingConfig)
data class AuditingConfig(val joins: Boolean = false, val leaves: Boolean = false, val webhook: String? = null)
data class QuickviewConfig(val furaffinityEnabled: Boolean = true, val furaffinityThumbnails: Boolean = false, val picartoEnabled: Boolean = true)
fun ServerConfig.toJSON(): String = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this)

object DatabaseOrchestrator {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private var configs = mutableMapOf<Long, ServerConfig>()
    private val dbUri = URI(System.getenv("DATABASE_URL"))
    private val username = dbUri.userInfo.split(":")[0]
    private val password = dbUri.userInfo.split(":")[1]
    private val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
    private val defaultConfig = ServerConfig(quickview = QuickviewConfig(), auditing = AuditingConfig())

    init {
        val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
        val ps = con.prepareStatement("SELECT * FROM serverconfigs") //TODO: Not select *
        val rs = ps.executeQuery()
        while (rs.next()) {
            this.configs[rs.getLong("guild_id")] = ServerConfig(
                    rs.getString("wiki"),
                    rs.getList("selectable_roles"),
                    QuickviewConfig(
                            rs.getBoolean("fa_quickview_enabled"),
                            rs.getBoolean("fa_quickview_thumbnail"),
                            rs.getBoolean("picarto_quickview_enabled")),
                    AuditingConfig(
                            rs.getBoolean("auditing_joins"),
                            rs.getBoolean("auditing_leaves"),
                            rs.getString("auditing_webhook"))
            )
        }
        con.close()
    }

    fun deleteServerConfig(guild: Guild) {
        try {
            this.configs.remove(guild.idLong)
            val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
            val ps = con.prepareStatement("DELETE FROM serverconfigs WHERE guild_id = ?")
            ps.setLong(1, guild.idLong)
            ps.executeQuery()
            con.commit()
            con.close()
        } catch (e: PSQLException) {
            log.debug("Failed to delete config for $guild! (Maybe it never existed?)")
        }
    }

    fun getServerConfig(guild: Guild) : ServerConfig {
        return configs.getOrDefault(guild.idLong,  defaultConfig)
    }

    fun getDefaultServerConfig() : ServerConfig {
        return defaultConfig
    }

    fun setServerConfig(guild: Guild, config: ServerConfig, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
            val ps = con.prepareStatement("INSERT INTO serverconfigs" +
                    " (guild_id, wiki, selectable_roles," +
                    " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                    " auditing_webhook, auditing_joins, auditing_leaves)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                    " ON CONFLICT (guild_id) DO UPDATE SET" +
                    " (wiki, selectable_roles," +
                    " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                    " auditing_webhook, auditing_joins, auditing_leaves)" +
                    " = (EXCLUDED.wiki, EXCLUDED.selectable_roles, " +
                    " EXCLUDED.fa_quickview_enabled, " +
                    " EXCLUDED.fa_quickview_thumbnail, EXCLUDED.picarto_quickview_enabled, " +
                    " EXCLUDED.auditing_webhook, EXCLUDED.auditing_joins, EXCLUDED.auditing_leaves)")
            ps.setLong(1, guild.idLong)
            ps.setString(2, config.wiki)
            ps.setList(3, config.selectableRoles.filterNotNull())
            ps.setBoolean(4, config.quickview.furaffinityEnabled)
            ps.setBoolean(5, config.quickview.furaffinityThumbnails)
            ps.setBoolean(6, config.quickview.picartoEnabled)
            ps.setString(7, config.auditing.webhook)
            ps.setBoolean(8, config.auditing.joins)
            ps.setBoolean(9, config.auditing.leaves)
            ps.executeUpdate()
            con.close()
            this.configs.replace(guild.idLong, config)
            onSuccess()
        } catch (e: Exception) {
            this.log.warn(e.message)
            onFailure(e)
        }
    }
}

val Guild.config : ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

//TODO: Something better than this
fun ResultSet.getList(columnLabel: String): List<String> { //This is probably the stupidest thing in the history of stupid things, maybe ever.
    return this.getArray(columnLabel).toString().removeSurrounding("{","}")
            .split(",").map { it.removeSurrounding("\"") }
}

fun PreparedStatement.setList(parameterIndex: Int, list: List<String>) {
    this.setArray(parameterIndex, this.connection.createArrayOf("text", list.toTypedArray()))
}