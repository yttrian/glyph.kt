package org.yttr.glyph.pubsub.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.channels.Channel
import org.yttr.glyph.Either
import org.yttr.glyph.left
import org.yttr.glyph.pubsub.PubSub
import org.yttr.glyph.pubsub.PubSubChannel
import org.yttr.glyph.pubsub.PubSubException
import org.yttr.glyph.right
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
                rendezvous.trySend(message)
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
