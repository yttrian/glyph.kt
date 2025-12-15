package org.yttr.glyph.bot.data

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

@OptIn(ExperimentalLettuceCoroutinesApi::class)
typealias RedisCoroutines = RedisCoroutinesCommands<String, String>

object Redis {
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    fun connect(uri: String): RedisCoroutines = RedisClient.create(uri).connect().coroutines()
}
