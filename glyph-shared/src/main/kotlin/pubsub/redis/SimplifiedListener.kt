/*
 * SimplifiedListener.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package org.yttr.glyph.shared.pubsub.redis

import io.lettuce.core.pubsub.RedisPubSubListener

internal abstract class SimplifiedListener : RedisPubSubListener<String, String> {
    final override fun message(pattern: String, channel: String, message: String) = Unit
    final override fun psubscribed(pattern: String, count: Long) = Unit
    final override fun punsubscribed(pattern: String, count: Long) = Unit
    final override fun unsubscribed(channel: String, count: Long) = Unit
    final override fun subscribed(channel: String, count: Long) = Unit

    abstract override fun message(channel: String, message: String)
}
