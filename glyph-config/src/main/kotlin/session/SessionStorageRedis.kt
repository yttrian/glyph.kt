package org.yttr.glyph.config.session

import io.ktor.sessions.SessionStorage
import io.ktor.util.toByteArray
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writer
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import java.time.Duration

/**
 * [SessionStorage] that stores session contents in Redis.
 *
 *
 */
class SessionStorageRedis(redisURI: RedisURI) : SessionStorage {
    private val redis = RedisClient.create().run {
        redisURI.apply {
            // We are using Heroku Redis which is version 5, but for some reason they give us a username.
            // However if we supply the username it runs the version 6 command and fails to login.
            username = null
        }
        connect(redisURI).async()
    }
    private val sessionTTLSeconds: Long = Duration.ofMinutes(SESSION_TTL_MINUTES).seconds

    override suspend fun invalidate(id: String) {
        redis.del("$SESSION_PREFIX:$id")
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R =
        redis.get("$SESSION_PREFIX:$id").await()?.let { data -> consumer(ByteReadChannel(data.encodeToByteArray())) }
            ?: throw NoSuchElementException("Session $id not found")

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        coroutineScope {
            val channel = writer(Dispatchers.Unconfined, autoFlush = true) {
                provider(channel)
            }.channel

            redis.setex("$SESSION_PREFIX:$id", sessionTTLSeconds, channel.toByteArray().decodeToString())
        }
    }

    companion object {
        private const val SESSION_PREFIX: String = "GlyphConfig:Session"
        private const val SESSION_TTL_MINUTES: Long = 30
    }
}
