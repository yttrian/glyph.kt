package org.yttr.glyph.shared.pubsub

/**
 * An issue with PubSub
 */
sealed class PubSubException : Exception() {
    /**
     * All subscribed listeners ignored the ask
     */
    object Ignored : PubSubException()

    /**
     * There are no listeners
     */
    object Deaf : PubSubException()
}
