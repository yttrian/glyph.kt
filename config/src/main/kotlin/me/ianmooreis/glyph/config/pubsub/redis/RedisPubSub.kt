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

package me.ianmooreis.glyph.config.pubsub.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.StringCodec
import kotlinx.coroutines.future.await
import me.ianmooreis.glyph.config.pubsub.PubSub

/**
 * PubSub implementation using Redis PubSub (sounds redundant, huh)
 */
class RedisPubSub(configure: Config.() -> Unit) : PubSub<String, String> {
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
    private val codec = StringCodec()
    private val redis = RedisClient.create().run {
        val redisUri = RedisURI.create(config.redisConnectionUri).apply {
            // We are using Heroku Redis which is version 5, but for some reason they give us a username.
            // However if we supply the username it runs the version 6 command and fails to login.
            username = null
        }
        connectPubSub(codec, redisUri)
    }
    private val singleMessageListener = SingleMessageListener(redis)

    override fun publish(channel: String, message: String) {
        redis.async().publish(channel, message)
    }

    override suspend fun listen(channel: String): String = singleMessageListener.await(channel)

    override suspend fun ask(message: String, outChannel: String, inChannel: String): String {
        require(outChannel != inChannel) {
            "Outbound channel must not be the same as the inbound channel!"
        }

        val listener = singleMessageListener.listen(inChannel)
        publish(outChannel, message)
        return listener.await()
    }
}
