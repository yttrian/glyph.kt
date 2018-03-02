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
        log.info("Registered: $skill")
        this.skills[skill.trigger] = skill
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

abstract class Skill(val trigger: String, private val serverOnly: Boolean = false,
                     private val requiredPermissionsUser: Collection<Permission> = emptyList(),
                     private val requiredPermissionsSelf: Collection<Permission> = emptyList()) {
    val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    open fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        this.log.warn("This skill does nothing!")
    }

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val permittedUser: Boolean = if (event.channelType.isGuild) event.member.hasPermission(requiredPermissionsUser) else true
        val permittedSelf: Boolean = if (event.channelType.isGuild) event.guild.selfMember.hasPermission(requiredPermissionsSelf) else true
        if ((serverOnly || requiredPermissionsUser.isNotEmpty()) && !event.channelType.isGuild ) {
            event.message.reply("You can only do this in a server!")
        } else if (!permittedSelf) {
            event.message.reply("I don't have the required permissions to do that! (${requiredPermissionsSelf.joinToString { it.name }})")
        } else if (!permittedUser) {
            event.message.reply("You don't have the required permissions to do that! (${requiredPermissionsUser.joinToString { it.name }})")
        } else {
            this.onTrigger(event, ai)
        }
    }

    override fun toString(): String = trigger
}