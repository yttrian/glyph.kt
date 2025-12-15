package org.yttr.glyph.bot.data

import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConfigStore private constructor(private val database: Database) : ConfigStore {
    override suspend fun getConfig(guild: Guild): ServerConfig = DatabaseServerConfig(guild.idLong, database)

    fun createTables() {
        transaction(db = database) {
            SchemaUtils.create(StarboardTable, QuickViewTable)
        }
    }

    companion object {
        fun create(database: Database): ConfigStore = DatabaseConfigStore(database).also { it.createTables() }
        fun create(jdbcDatabaseUrl: String): ConfigStore = create(Database.connect(jdbcDatabaseUrl))
    }
}
