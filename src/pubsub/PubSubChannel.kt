package org.yttr.glyph.pubsub

/**
 * Channel names and prefixes for PubSub
 */
enum class PubSubChannel(
    /**
     * The value associated with the enum item
     */
    val value: String
) {
    /**
     * Send a server ID to this channel to refresh the cached config for it
     */
    CONFIG_REFRESH("Glyph:Config:Refresh"),

    /**
     * Used for ask and responder
     */
    CONFIG_PREFIX("Glyph:Config")
}
