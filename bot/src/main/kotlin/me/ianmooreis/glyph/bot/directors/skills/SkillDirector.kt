/*
 * SkillDirector.kt
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

package me.ianmooreis.glyph.bot.directors.skills

import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.bot.ai.AIResponse
import me.ianmooreis.glyph.bot.messaging.Response
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Manages all the available skills
 */
class SkillDirector : Director() {
    private val skills: MutableMap<String, Skill> = mutableMapOf()
    private val cooldowns = mutableMapOf<Pair<Long, String>, SkillCooldown>()

    private fun addSkill(skill: Skill): SkillDirector {
        log.debug("Registered: $skill")
        skills[skill.trigger] = skill.also { it.skillDirector = this }
        return this
    }

    /**
     * Add a skill to the list of skills
     *
     * @param skills skills to be registered as available
     */
    fun addSkill(vararg skills: Skill): SkillDirector {
        skills.distinct().forEach { addSkill(it) }
        log.info("Registered ${skills.size} skills")
        return this
    }

    /**
     * Sets a cooldown for a user for a skill
     *
     * @param user  the user to cooldown
     * @param skill the skill that the user is being cooled on
     * @param cooldown the cooldown the user has
     */
    fun setCooldown(user: User, skill: Skill, cooldown: SkillCooldown) {
        cooldowns[Pair(user.idLong, skill.trigger)] = cooldown
    }

    /**
     * Gets a cooldown for a user for a skill
     *
     * @param user  the user who's cooldown is being grabbed
     * @param skill the skill that the user's cooldown is being grabbed on
     */
    fun getCooldown(user: User, skill: Skill): SkillCooldown? {
        return cooldowns[Pair(user.idLong, skill.trigger)]
    }

    /**
     * Trigger a skill via its trigger name based on a message event and DialogFlow response
     *
     * @param event the message event
     * @param ai    the DialogFlow response
     */
    suspend fun trigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val result = ai.result
        val action = result.action
        val skill: Skill? = skills[action]
        return if (skill != null && !ai.result.isActionIncomplete) {
            skill.trigger(event, ai)
        } else {
            Response.Volatile(
                if (result.fulfillment.speech.isEmpty()) {
                    "`$action` is not available yet!"
                } else {
                    result.fulfillment.speech
                }
            )
        }
    }
}
