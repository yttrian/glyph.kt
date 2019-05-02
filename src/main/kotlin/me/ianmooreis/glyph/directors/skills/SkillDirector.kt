/*
 * SkillDirector *
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

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

/**
 * Manages all the available skills
 */
object SkillDirector : ListenerAdapter() {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val skills: MutableMap<String, Skill> = mutableMapOf()
    private val cooldowns = mutableMapOf<Pair<Long, String>, SkillCooldown>()

    private fun addSkill(skill: Skill): SkillDirector {
        log.debug("Registered: $skill")
        skills[skill.trigger] = skill
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
    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val result = ai.result
        val action = result.action
        val skill: Skill? = skills[action]
        if (skill != null && !ai.result.isActionIncomplete) {
            skill.trigger(event, ai)
        } else {
            event.message.reply(
                if (result.fulfillment.speech.isEmpty()) {
                    "`$action` is not available yet!"
                } else {
                    result.fulfillment.speech.replace("\\n", "\n")
                }
            )
        }
    }
}

