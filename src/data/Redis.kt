package org.yttr.glyph.data

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

/**
 * Reduces the need to type out the long type for Redis Async String Commands
 */
typealias RedisAsync = RedisAsyncCommands<String, String>

/**
 * Lock a Redlock locked value for a fixed duration.
 * https://redis.io/topics/distlock
 */
suspend fun RedisAsync.redlockLock(key: String, value: String, ttlSeconds: Long): Boolean =
    set(key, value, SetArgs().ex(ttlSeconds).nx()).await() == "OK"

/**
 * Unlock a Redlock locked value if it still matches the expected value.
 * https://redis.io/topics/distlock
 */
suspend fun RedisAsync.redlockUnlock(key: String, expectedValue: String): Boolean = eval<Long>(
    """
        if redis.call("get", KEYS[1]) == ARGV[1]
        then
            return redis.call("del", KEYS[1])
        else
            return 0
        end
    """.trimIndent(),
    ScriptOutputType.INTEGER,
    arrayOf(key),
    expectedValue
).await() != 0L
