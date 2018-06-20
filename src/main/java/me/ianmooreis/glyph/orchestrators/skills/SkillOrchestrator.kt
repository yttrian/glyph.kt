package me.ianmooreis.glyph.orchestrators.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

/**
 * Manages all the available skills
 */
object SkillOrchestrator {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val skills: MutableMap<String, Skill> = mutableMapOf()
    private val cooldowns = mutableMapOf<Pair<Long, String>, SkillCooldown>()

    private fun addSkill(skill: Skill): SkillOrchestrator {
        log.debug("Registered: $skill")
        skills[skill.trigger] = skill
        return this
    }

    /**
     * Add a skill to the list of skills
     *
     * @param skills skills to be registered as available
     */
    fun addSkill(vararg skills: Skill): SkillOrchestrator {
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
            event.message.reply(if (result.fulfillment.speech.isEmpty()) {
                "`$action` is not available yet!"
            } else {
                result.fulfillment.speech.replace("\\n", "\n")
            })
        }
    }
}

