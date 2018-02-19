package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.entities.Guild
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

data class ServerConfig(val wiki: String = "wikipedia", val selectableRoles: List<String> = emptyList(),
                        val spoilersChannel: String? = null, val spoilersKeywords: List<String> = emptyList(),
                        val faQuickviewEnabled: Boolean = true, val faQuickviewThumbnail: Boolean = false, val picartoQuickviewEnabled: Boolean = true,
                        val auditingJoins: Boolean = false, val auditingLeaves: Boolean = false, val auditingChannel: String? = null,
                        val lang: String = "en")
fun ServerConfig.toJSON(): JSONObject {
    /*val prettyPrint = mapOf(
        "auditing" to mapOf("channel" to this.auditingChannel, "joins" to this.auditingJoins, "leaves" to this.auditingLeaves),
        "quickview" to mapOf("fa" to mapOf("enabled" to this.faQuickviewEnabled, "thumbnail" to this.faQuickviewThumbnail), "picarto" to mapOf("enabled" to this.picartoQuickviewEnabled)),
        "roles" to mapOf("selectable" to this.selectableRoles),
        "spoilers" to mapOf("keywords" to this.spoilersKeywords, "safeChannel" to this.spoilersChannel),
        "wiki" to this.wiki
    )*/ //TODO: Reinvestigation pretty printing later
    return JSONObject(this)
}

object DatabaseOrchestrator {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private var configs = mutableMapOf<Long, ServerConfig>()
    private val dbUri = URI(System.getenv("DATABASE_URL"))
    private val username = dbUri.userInfo.split(":")[0]
    private val password = dbUri.userInfo.split(":")[1]
    private val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
    private val defaultConfig = ServerConfig()

    init {
        val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
        val ps = con.prepareStatement("SELECT * FROM serverconfigs") //TODO: Not select *
        val rs = ps.executeQuery()
        while (rs.next()) {
            this.configs[rs.getLong("guild_id")] = ServerConfig(
                    rs.getString("wiki"),
                    rs.getList("selectable_roles"),
                    rs.getString("spoilers_channel"),
                    rs.getList("spoilers_keywords"),
                    rs.getBoolean("fa_quickview_enabled"),
                    rs.getBoolean("fa_quickview_thumbnail"),
                    rs.getBoolean("picarto_quickview_enabled"),
                    rs.getBoolean("auditing_joins"),
                    rs.getBoolean("auditing_leaves"),
                    rs.getString("auditing_channel"),
                    rs.getString("lang"))
        }
        con.close()
    }

    fun deleteServerConfig(guild: Guild) {
        this.configs.remove(guild.idLong)
        val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
        val ps = con.prepareStatement("DELETE FROM serverconfigs WHERE guild_id = ?")
        ps.setLong(1, guild.idLong)
        ps.executeQuery()
        con.commit()
        con.close()
    }

    fun getServerConfig(guild: Guild) : ServerConfig {
        return configs.getOrDefault(guild.idLong,  defaultConfig)
    }

    fun setServerConfig(guild: Guild, config: ServerConfig, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val con = DriverManager.getConnection(this.dbUrl, this.username, this.password)
            val ps = con.prepareStatement("INSERT INTO serverconfigs" +
                    " (guild_id, wiki, selectable_roles, spoilers_channel, spoilers_keywords," +
                    " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                    " auditing_channel, auditing_joins, auditing_leaves)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                    " ON CONFLICT (guild_id) DO UPDATE SET" +
                    " (wiki, selectable_roles, spoilers_channel, spoilers_keywords," +
                    " fa_quickview_enabled, fa_quickview_thumbnail, picarto_quickview_enabled, " +
                    " auditing_channel, auditing_joins, auditing_leaves)" +
                    " = (EXCLUDED.wiki, EXCLUDED.selectable_roles, EXCLUDED.spoilers_channel, " +
                    " EXCLUDED.spoilers_keywords, EXCLUDED.fa_quickview_enabled, " +
                    " EXCLUDED.fa_quickview_thumbnail, EXCLUDED.picarto_quickview_enabled, " +
                    " EXCLUDED.auditing_channel, EXCLUDED.auditing_joins, EXCLUDED.auditing_leaves)")
            ps.setLong(1, guild.idLong)
            ps.setString(2, config.wiki)
            ps.setList(3, config.selectableRoles)
            ps.setString(4, config.spoilersChannel)
            ps.setList(5, config.spoilersKeywords)
            ps.setBoolean(6, config.faQuickviewEnabled)
            ps.setBoolean(7, config.faQuickviewThumbnail)
            ps.setBoolean(8, config.picartoQuickviewEnabled)
            ps.setString(9, config.auditingChannel)
            ps.setBoolean(10, config.auditingJoins)
            ps.setBoolean(11, config.auditingLeaves)
            ps.executeUpdate()
            con.close()
            this.configs.replace(guild.idLong, config)
            onSuccess()
        } catch (e: Exception) {
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