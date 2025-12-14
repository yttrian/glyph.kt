package org.yttr.glyph.bot

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

@OptIn(ExperimentalLettuceCoroutinesApi::class)
typealias RedisCoroutines = RedisCoroutinesCommands<String, String>
