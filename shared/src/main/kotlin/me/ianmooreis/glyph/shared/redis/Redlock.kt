/*
 * Redlock.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package me.ianmooreis.glyph.shared.redis

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import kotlinx.coroutines.future.await

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
