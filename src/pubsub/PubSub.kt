package org.yttr.glyph.pubsub

import org.yttr.glyph.Either

/**
 * Generic interface for PubSub connectors
 */
interface PubSub {
    /**
     * Publish a message
     */
    fun publish(channel: PubSubChannel, message: String)

    /**
     * Add an action-less listener
     */
    fun addListener(listenChannel: PubSubChannel, action: (message: String) -> Unit)

    /**
     * Publish a message and listen for the response
     */
    suspend fun ask(query: String, askChannelPrefix: PubSubChannel): Either<PubSubException, String>

    /**
     * Add a responder for an ask
     */
    fun addResponder(askChannelPrefix: PubSubChannel, responder: (message: String) -> String?)
}
