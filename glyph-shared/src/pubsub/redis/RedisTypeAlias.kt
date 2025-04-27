package org.yttr.glyph.shared.pubsub.redis

import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection

/**
 * Reduces the need to type out the long type for Redis Async String Commands
 */
typealias RedisAsync = RedisAsyncCommands<String, String>

/**
 * Reduces the need to type out the long type for Stateful Redis PubSub Connection
 */
typealias StatefulRedisPubSub = StatefulRedisPubSubConnection<String, String>
