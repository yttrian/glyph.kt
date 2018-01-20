package me.ianmooreis.glyph.orchestrators

import ai.api.model.AIResponse
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

object SkillOrchestrator {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private var skills: MutableMap<String, Skill> = mutableMapOf()

    fun addSkill(skill: Skill): SkillOrchestrator {
        log.info("Registered: ${skill.trigger}")
        this.skills.put(skill.trigger, skill)
        return this
    }

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        println(this.skills)
        val result = ai.result
        val action = result.action
        val skill: Skill? = this.skills[action]
        if (skill != null) {
            skill.onTrigger(event, ai)
        } else {
            event.message.reply(if (result.fulfillment.speech.isEmpty()) "`$action` is not available yet!" else result.fulfillment.speech)
        }
    }
}

abstract class Skill(val trigger: String) {
    open fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {

    }
}