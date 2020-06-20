/*
 * RedisPubSub.kt
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

package me.ianmooreis.glyph.shared.pubsub.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import me.ianmooreis.glyph.shared.pubsub.PubSub
import me.ianmooreis.glyph.shared.pubsub.PubSubChannel

/**
 * PubSub implementation using Redis PubSub (sounds redundant, huh)
 */
class RedisPubSub(configure: Config.() -> Unit) : PubSub {
    /**
     * HOCON-like config for Redis PubSub setup
     */
    class Config {
        /**
         * A uri that describes how to connect to the Redis instance
         */
        var redisConnectionUri: String = "redis://localhost"
    }

    private val config = Config().also(configure)
    private val redis = RedisClient.create(RedisURI.create(config.redisConnectionUri).apply { username = null })
    private val redisCommandsAsync = redis.connect().async()
    private val redisPubSubConnection = redis.connectPubSub()
    private val singleMessageListener = SingleMessageListener(redisPubSubConnection)

    override fun publish(channel: PubSubChannel, message: String) {
        redisCommandsAsync.publish(channel.value, message)
    }

    override fun addListener(listenChannel: PubSubChannel, action: (message: String) -> Unit) {
        redisPubSubConnection.addListener(object : SimplifiedListener() {
            override fun message(channel: String, message: String) {
                if (channel == listenChannel.value) {
                    action(message)
                }
            }
        })
    }

    override suspend fun ask(query: String, askChannelPrefix: PubSubChannel): String {
        val listener = singleMessageListener.listen(askChannelPrefix.asResponse(query))
        redisCommandsAsync.publish(askChannelPrefix.asQuery, query)
        return listener.receive()
    }

    override fun addResponder(askChannelPrefix: PubSubChannel, responder: (message: String) -> String) {
        redisPubSubConnection.addListener(object : SimplifiedListener() {
            override fun message(channel: String, message: String) {
                if (channel == askChannelPrefix.asQuery) {
                    redisCommandsAsync.publish(askChannelPrefix.asResponse(message), responder(message))
                }
            }
        })

        redisPubSubConnection.async().subscribe(askChannelPrefix.asQuery)
    }

    private val PubSubChannel.asQuery
        get() = this.value + ":Query"

    private fun PubSubChannel.asResponse(query: String) = this.value + ":Response:" + query
}
