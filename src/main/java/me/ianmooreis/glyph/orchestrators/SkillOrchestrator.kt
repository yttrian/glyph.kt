package me.ianmooreis.glyph.orchestrators

import ai.api.model.AIResponse
import net.dv8tion.jda.core.Permission
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
        val result = ai.result
        val action = result.action
        val skill: Skill? = this.skills[action]
        if (skill != null) {
            skill.trigger(event, ai)
        } else {
            event.message.reply(if (result.fulfillment.speech.isEmpty()) "`$action` is not available yet!" else result.fulfillment.speech)
        }
    }
}

abstract class Skill(val trigger: String, private val serverOnly: Boolean = false, private val requiredPermissions: Collection<Permission> = emptyList()) {
    val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    open fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        this.log.warn("This skill does nothing!")
    }

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val permitted: Boolean = event.member?.hasPermission(requiredPermissions) ?: true
        if ((serverOnly || requiredPermissions.isNotEmpty()) && !event.channel.type.isGuild ) {
            event.message.reply("You can only do this in a server!")
        } else if (!permitted) {
            event.message.reply("You don't have the required permissions to do that! (${requiredPermissions.joinToString { it.name }})")
        } else {
            this.onTrigger(event, ai)
        }
    }
}