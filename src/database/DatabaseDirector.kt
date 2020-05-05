/*
 * DatabaseDirector.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package me.ianmooreis.glyph.database

import me.ianmooreis.glyph.Director
import org.jetbrains.exposed.sql.Database
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.net.URI

/**
 * A director that encapsulates database related functionality
 */
class DatabaseDirector(configure: Config.() -> Unit) : Director() {
    /**
     * HOCON-like config for the database director
     */
    class Config {
        /**
         * A url the describes how to connect to the main database
         */
        var databaseConnectionUrl: String = "localhost"

        /**
         * The driver used when connecting to the database, usually Postgres
         */
        var driver: String = "org.postgresql.Driver"

        /**
         * A url that describes how to connect to the Redis instance
         */
        var redisConnectionUrl: String = "localhost"

        /**
         * The maximum total connections allowed in the Redis pool
         */
        var redisMaxTotal: Int = 10
    }

    private val config = Config().also(configure)
    private val db = Database.apply {
        val dbUri = URI(config.databaseConnectionUrl)
        val userInfo = dbUri.userInfo.split(":")
        val username = userInfo[0]
        val password = userInfo[1]
        val connectionUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"

        connect(
            connectionUrl, driver = config.driver,
            user = username, password = password
        )
    }

    /**
     * The Redis client, to be used for interacting with Redis
     */
    val redisPool: JedisPool by lazy {
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = config.redisMaxTotal
        }
        JedisPool(poolConfig, URI(config.redisConnectionUrl))
    }
}