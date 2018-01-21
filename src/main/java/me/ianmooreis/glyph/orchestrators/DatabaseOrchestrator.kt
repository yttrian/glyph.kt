package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.entities.Guild
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.jdbc.PgArray
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI

object ServerConfigs : Table() {
    class StringArrayColumnType : ColumnType() { override fun sqlType(): String = "TEXT[]" }
    private fun list(name: String): Column<PgArray> = registerColumn(name, StringArrayColumnType())
    class BigIntType : ColumnType() { override fun sqlType(): String = "BIGINT" }
    private fun bigint(name: String): Column<Long> = registerColumn(name, BigIntType())

    val guild_id = bigint("guild_id").primaryKey()
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

data class ServerConfig(val wiki: String, val selectable_roles: List<String>,
                        val spoilers_channel: String?, val spoilers_keywords: List<String>,
                        val fa_quickview_enabled: Boolean, val fa_quickview_thumbnail: Boolean, val picarto_quickview_enabled: Boolean,
                        val auditing_joins: Boolean, val auditing_leaves: Boolean, val auditing_reactions: Boolean, val auditing_channel: String?,
                        val lang: String)

object DatabaseOrchestrator {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private var configs = mutableMapOf<String, ServerConfig>()
    val defaultConfig = ServerConfig("wikipedia", emptyList(),
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
                configs.put(
                        config[ServerConfigs.guild_id].toString(),
                        ServerConfig(config[ServerConfigs.wiki], config[ServerConfigs.selectable_roles].toList(),
                                config[ServerConfigs.spoilers_channel], config[ServerConfigs.spoilers_keywords].toList(),
                                config[ServerConfigs.fa_quickview_enabled], config[ServerConfigs.fa_quickview_thumbnail], config[ServerConfigs.picarto_quickview_enabled],
                                config[ServerConfigs.auditing_joins], config[ServerConfigs.auditing_leaves], config[ServerConfigs.auditing_reactions], config[ServerConfigs.auditing_channel],
                                config[ServerConfigs.lang]))
            }
        }
    }
    fun getServerConfig(guild: Guild) : ServerConfig {
        return configs.getOrDefault(guild.id, defaultConfig)
    }
    fun updateServerConfig(guild: Guild, config: ServerConfig) {
        // TODO: Use or make an UPSERT instead of this mess
        transaction {
            val result = ServerConfigs.update({ ServerConfigs.guild_id eq guild.idLong}) {
                it[wiki] = config.wiki
                //it[selectable_roles] = config.selectable_roles
                it[spoilers_channel] = config.spoilers_channel ?: ""
                //it[spoilers_keywords] = config.spoilers_keywords
                it[fa_quickview_enabled] = config.fa_quickview_enabled
                it[fa_quickview_thumbnail] = config.fa_quickview_thumbnail
                it[picarto_quickview_enabled] = config.picarto_quickview_enabled
                it[auditing_joins] = config.auditing_joins
                it[auditing_leaves] = config.auditing_leaves
                it[auditing_reactions] = config.auditing_reactions
                it[auditing_channel] = config.auditing_channel ?: ""
                it[lang] = config.lang
            }
            if (result == 0) { //0 means it failed, kinda seems to be a dumb return value
                ServerConfigs.insert {
                    it[guild_id] = guild.idLong
                    it[wiki] = config.wiki
                    //it[selectable_roles] = config.selectable_roles
                    it[spoilers_channel] = config.spoilers_channel ?: ""
                    //it[spoilers_keywords] = config.spoilers_keywords
                    it[fa_quickview_enabled] = config.fa_quickview_enabled
                    it[fa_quickview_thumbnail] = config.fa_quickview_thumbnail
                    it[picarto_quickview_enabled] = config.picarto_quickview_enabled
                    it[auditing_joins] = config.auditing_joins
                    it[auditing_leaves] = config.auditing_leaves
                    it[auditing_reactions] = config.auditing_reactions
                    it[auditing_channel] = config.auditing_channel ?: ""
                    it[lang] = config.lang
                }
            }
        }
    }
    fun test(){
        log.info(this.configs.toString())
    }
}

val Guild.config : ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

//TODO: Something better than this
fun PgArray.toList() : List<String> { //This is probably the stupidest thing in the history of stupid things, maybe ever.
    return this.toString().removeSurrounding("{","}")
            .split(",").map { it.removeSurrounding("\"") }
}