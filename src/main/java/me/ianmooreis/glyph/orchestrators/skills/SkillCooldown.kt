package me.ianmooreis.glyph.orchestrators.skills

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