package me.ianmooreis.glyph

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.jdbc.PgArray
import java.net.URI

object ServerConfigs : Table() {
    class StringArrayColumnType : ColumnType() { override fun sqlType(): String = "TEXT[]" }
    private fun list(name: String): Column<PgArray> = registerColumn(name, StringArrayColumnType())

    val id = long("guild_id").primaryKey()
    val wiki = text("wiki")
    val selectable_roles = list("selectable_roles")
    val spoilers_channel = text("spoilers_channel")
    val spoilers_keywords = list("spoilers_keywords")
    val fa_quickview_enabled = bool("fa_quickview_enabled")
    val fa_quickview_thumbnail = bool("fa_quickview_thumbnail")
    val picarto_quickview_enabled = bool("picarto_quickview_enabled")
    val auditing_joins = bool("auditing_joins")
    val auditing_leaves = bool("auditing_leaves")
    val auditing_reactions = bool("auditing_reactions")
    val auditing_channel = text("auditing_channel")
    val lang = text("lang")
}

data class ServerConfig(val wiki: String, val selectable_roles: Any,
                        val spoilers_channel: String?, val spoilers_keywords: Any,
                        val fa_quickview_enabled: Boolean, val fa_quickview_thumbnail: Boolean, val picarto_quickview_enabled: Boolean,
                        val auditing_joins: Boolean, val auditing_leaves: Boolean, val auditing_reactions: Boolean, val auditing_channel: String?,
                        val lang: String)

object DatabaseOrchestrator {
    private var configs = mutableMapOf<String, ServerConfig>()
    val defaultConfig = ServerConfig("wikipedia", emptyList<String>(),
        null, emptyList<String>(),
        true, false, true,
        false, false, false, null,
        "en")
    init {
        val dbUri = URI(System.getenv("DATABASE_URL"))
        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]
        val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = username, password = password)
        transaction {
            for (config in ServerConfigs.selectAll()) {
                this@DatabaseOrchestrator.configs.put(
                        config[ServerConfigs.id].toString(),
                        ServerConfig(config[ServerConfigs.wiki], config[ServerConfigs.selectable_roles],
                                config[ServerConfigs.spoilers_channel], config[ServerConfigs.spoilers_keywords],
                                config[ServerConfigs.fa_quickview_enabled],config[ServerConfigs.fa_quickview_thumbnail], config[ServerConfigs.picarto_quickview_enabled],
                                config[ServerConfigs.auditing_joins], config[ServerConfigs.auditing_leaves], config[ServerConfigs.auditing_reactions], config[ServerConfigs.auditing_channel],
                                config[ServerConfigs.lang]))
            }
        }
    }
    fun getServerConfig(guild: Guild) : ServerConfig {
        return configs.getOrDefault(guild.id, defaultConfig)
    }
    fun updateServerConfig(guild: Guild, config: ServerConfig) {
        transaction {
            ServerConfigs.update({ServerConfigs.id eq guild.id}) {

            }
        }
    }
    fun test(){
        println(configs)
    }
}

fun Message.getConfig() : ServerConfig {
    return when (this.guild != null) {
        true -> DatabaseOrchestrator.defaultConfig
        false -> DatabaseOrchestrator.getServerConfig(this.guild)
    }
}
