/*
 * SkillCooldown.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors.skills

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * A cooldown for a skill
 */
class SkillCooldown(duration: Long, unit: TimeUnit) {
    private val endTime: Instant = Instant.now().plus(duration, unit.toChronoUnit())
    private var wasWarned: Boolean = false

    /**
     * Whether or not the cooldown has expired yet
     */
    val expired: Boolean
        get() = Instant.now().isAfter(endTime) || remainingSeconds.toInt() == 0

    /**
     * Get the remaining amount of time in seconds for the cooldown
     */
    val remainingSeconds: Long
        get() = Duration.between(Instant.now(), endTime).toSeconds()

    /**
     * Whether or not the user has been warned about the cooldown yet
     */
    var warned: Boolean
        get() = wasWarned
        set(value) {
            wasWarned = value
        }
}