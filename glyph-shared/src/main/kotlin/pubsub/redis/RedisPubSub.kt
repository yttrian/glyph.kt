/*
 * RedisPubSub.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.shared.pubsub.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.channels.Channel
import org.yttr.glyph.shared.Either
import org.yttr.glyph.shared.left
import org.yttr.glyph.shared.pubsub.PubSub
import org.yttr.glyph.shared.pubsub.PubSubChannel
import org.yttr.glyph.shared.pubsub.PubSubException
import org.yttr.glyph.shared.right
import java.util.concurrent.atomic.AtomicLong

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
    private val redisPubSubConnection: StatefulRedisPubSub = redis.connectPubSub()

    override fun publish(channel: PubSubChannel, message: String) {
        redisCommandsAsync.publish(channel.value, message)
    }

    override fun addListener(listenChannel: PubSubChannel, action: (message: String) -> Unit): Unit =
        redisPubSubConnection.addListener(listenChannel.value, action)

    override suspend fun ask(query: String, askChannelPrefix: PubSubChannel): Either<PubSubException, String> {
        val rendezvous = Channel<Either<PubSubException, String>>()
        val failMax = AtomicLong()

        val failingResponse = askChannelPrefix.asFailResponse(query)
        val successfulResponse = askChannelPrefix.asSuccessResponse(query)

        val listener = object : SimplifiedListener() {
            override fun message(channel: String, message: String) {
                when (channel) {
                    successfulResponse -> complete(message.right())
                    failingResponse -> if (failMax.decrementAndGet() <= 0) {
                        complete(PubSubException.Ignored.left())
                    }
                }
            }

            fun complete(message: Either<PubSubException, String>) {
                redisPubSubConnection.async().unsubscribe(successfulResponse, failingResponse)
                redisPubSubConnection.removeListener(this)
                rendezvous.offer(message)
                rendezvous.close()
            }
        }

        redisPubSubConnection.addListener(listener)
        redisPubSubConnection.async().subscribe(successfulResponse, failingResponse)
        redisCommandsAsync.publish(askChannelPrefix.asQuery, query).thenAccept {
            if (it == 0.toLong()) {
                listener.complete(PubSubException.Deaf.left())
            }
            failMax.set(it)
        }

        return rendezvous.receive()
    }

    override fun addResponder(askChannelPrefix: PubSubChannel, responder: (message: String) -> String?) {
        redisPubSubConnection.addListener(askChannelPrefix.asQuery) { message ->
            val result = responder(message)
            val responseChannel = if (result != null) {
                askChannelPrefix.asSuccessResponse(message)
            } else {
                askChannelPrefix.asFailResponse(message)
            }
            redisCommandsAsync.publish(responseChannel, result)
        }
    }

    private fun StatefulRedisPubSub.addListener(
        listenChannel: String,
        listener: (message: String) -> Unit
    ) = this.addListener(object : SimplifiedListener() {
        override fun message(channel: String, message: String) {
            if (channel == listenChannel) {
                listener(message)
            }
        }
    }).also { async().subscribe(listenChannel) }

    private val PubSubChannel.asQuery
        get() = this.value + ":Query"

    private fun PubSubChannel.asFailResponse(query: String) = this.value + ":Response:" + query + ":Fail"

    private fun PubSubChannel.asSuccessResponse(query: String) = this.value + ":Response:" + query + ":Success"
}
