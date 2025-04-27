package org.yttr.glyph.config.session

import io.ktor.server.sessions.SessionStorage
import io.lettuce.core.RedisClient
import kotlinx.coroutines.future.await
import java.time.Duration

/**
 * [SessionStorage] that stores session contents in Redis.
 */
class SessionStorageRedis(redisUrl: String) : SessionStorage {
    private val redis = RedisClient.create(redisUrl).connect().async()
    private val sessionTTLSeconds: Long = Duration.ofMinutes(SESSION_TTL_MINUTES).seconds

    override suspend fun invalidate(id: String) {
        redis.del("$SESSION_PREFIX:$id")
    }

    override suspend fun read(id: String): String {
        return redis.get("$SESSION_PREFIX:$id")?.await() ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun write(id: String, value: String) {
        redis.setex("$SESSION_PREFIX:$id", sessionTTLSeconds, value)
    }

    companion object {
        private const val SESSION_PREFIX: String = "GlyphConfig:Session"
        private const val SESSION_TTL_MINUTES: Long = 30
    }
}
