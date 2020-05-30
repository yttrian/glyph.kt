/*
 * SingleMessageListener.kt
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

import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.channels.Channel

/**
 * Provides an interface for listening for the next message on a channel
 */
internal class SingleMessageListener<K, V>(
    /**
     * Redis PubSub connection for adding/removing the listener
     */
    private val redis: StatefulRedisPubSubConnection<K, V>
) {
    /**
     * Get CompletableFuture for response channel
     */
    fun listen(responseChannel: K): Channel<V> {
        val rendezvous = Channel<V>()
        val listener = object : RedisPubSubListener<K, V> {
            override fun message(pattern: K?, channel: K?, message: V?) = Unit
            override fun psubscribed(pattern: K?, count: Long) = Unit
            override fun punsubscribed(pattern: K?, count: Long) = Unit
            override fun unsubscribed(channel: K?, count: Long) = Unit
            override fun subscribed(channel: K?, count: Long) = Unit

            override fun message(channel: K, message: V) {
                if (channel == responseChannel) {
                    redis.async().unsubscribe(channel)
                    redis.removeListener(this)
                    rendezvous.offer(message)
                }
            }
        }

        redis.addListener(listener)
        redis.async().subscribe(responseChannel)

        return rendezvous
    }

    /**
     * Wait for the message
     */
    suspend fun await(responseChannel: K): V = listen(responseChannel).receive()
}
