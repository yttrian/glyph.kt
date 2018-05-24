package me.ianmooreis.glyph.orchestrators.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

object SkillOrchestrator {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val skills: MutableMap<String, SkillAdapter> = mutableMapOf()
    private val cooldowns = mutableMapOf<Pair<Long, String>, SkillCooldown>()

    private fun addSkill(skill: SkillAdapter): SkillOrchestrator {
        log.debug("Registered: $skill")
        skills[skill.trigger] = skill
        return this
    }

    fun addSkill(vararg skills: SkillAdapter): SkillOrchestrator {
        skills.distinct().forEach { addSkill(it) }
        log.info("Registered ${skills.size} skills")
        return this
    }

    fun setCooldown(user: User, skill: SkillAdapter, cooldown: SkillCooldown) {
        cooldowns[Pair(user.idLong, skill.trigger)] = cooldown
    }

    fun getCooldown(user: User, skill: SkillAdapter): SkillCooldown? {
        return cooldowns[Pair(user.idLong, skill.trigger)]
    }

    fun removeCooldown(user: User, skill: SkillAdapter) {
        cooldowns.remove(Pair(user.idLong, skill.trigger))
    }

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val result = ai.result
        val action = result.action
        val skill: SkillAdapter? = skills[action]
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

