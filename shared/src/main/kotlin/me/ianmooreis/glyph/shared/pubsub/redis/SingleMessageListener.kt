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

package me.ianmooreis.glyph.shared.pubsub.redis

import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.channels.Channel

/**
 * Provides an interface for listening for the next message on a channel
 */
internal class SingleMessageListener(
    /**
     * Redis PubSub connection for adding/removing the listener
     */
    private val redis: StatefulRedisPubSubConnection<String, String>
) {
    /**
     * Get CompletableFuture for response channel
     */
    fun listen(responseChannel: String): Channel<String> {
        val rendezvous = Channel<String>()
        val listener = object : SimplifiedListener() {
            override fun message(channel: String, message: String) {
                if (channel == responseChannel) {
                    redis.async().unsubscribe(channel)
                    redis.removeListener(this)
                    rendezvous.offer(message)
                    rendezvous.close()
                }
            }
        }

        redis.addListener(listener)
        redis.async().subscribe(responseChannel)

        return rendezvous
    }
}
