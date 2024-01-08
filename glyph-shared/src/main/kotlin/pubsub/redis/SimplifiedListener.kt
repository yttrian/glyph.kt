package org.yttr.glyph.shared.pubsub.redis

import io.lettuce.core.pubsub.RedisPubSubListener

internal abstract class SimplifiedListener : RedisPubSubListener<String, String> {
    final override fun message(pattern: String, channel: String, message: String) = Unit
    final override fun psubscribed(pattern: String, count: Long) = Unit
    final override fun punsubscribed(pattern: String, count: Long) = Unit
    final override fun unsubscribed(channel: String, count: Long) = Unit
    final override fun subscribed(channel: String, count: Long) = Unit

    abstract override fun message(channel: String, message: String)
}
