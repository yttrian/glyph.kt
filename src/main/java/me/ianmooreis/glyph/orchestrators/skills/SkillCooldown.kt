package me.ianmooreis.glyph.orchestrators.skills

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class SkillCooldown(duration: Long, unit: TimeUnit) {
    private val endTime: Instant = Instant.now().plus(duration, unit.toChronoUnit())
    private var wasWarned: Boolean = false

    val expired: Boolean
        get() = Instant.now().isAfter(endTime)

    val remainingSeconds: Long
        get() = Duration.between(Instant.now(), endTime).toSeconds()

    var warned: Boolean
        get() = wasWarned
        set(value) {
            wasWarned = value
        }
}