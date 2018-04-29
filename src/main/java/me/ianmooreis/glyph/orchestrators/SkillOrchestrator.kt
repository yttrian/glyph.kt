package me.ianmooreis.glyph.orchestrators

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

object SkillOrchestrator {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private var skills: MutableMap<String, SkillAdapter> = mutableMapOf()

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

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val result = ai.result
        val action = result.action
        val skill: SkillAdapter? = skills[action]
        if (skill != null && !ai.result.isActionIncomplete) {
            skill.trigger(event, ai)
        } else {
            event.message.reply(if (result.fulfillment.speech.isEmpty()) "`$action` is not available yet!" else result.fulfillment.speech)
        }
    }
}

abstract class SkillAdapter(val trigger: String, private val guildOnly: Boolean = false,
                            private val requiredPermissionsUser: Collection<Permission> = emptyList(),
                            private val requiredPermissionsSelf: Collection<Permission> = emptyList(),
                            private val creatorOnly: Boolean = false) {
    val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    open fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        this.log.warn("This skill does nothing!")
    }

    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val permittedUser: Boolean = if (event.channelType.isGuild) event.member.hasPermission(requiredPermissionsUser) else true
        val permittedSelf: Boolean = if (event.channelType.isGuild) event.guild.selfMember.hasPermission(requiredPermissionsSelf) else true
        when {
            ((guildOnly || requiredPermissionsUser.isNotEmpty()) && !event.channelType.isGuild) -> event.message.reply("${CustomEmote.XMARK} You can only do that in a server!")
            !permittedSelf -> event.message.reply("${CustomEmote.XMARK} I don't have the required permissions to do that! (${requiredPermissionsSelf.joinToString { prettyPrintPermissionName(it) }})")
            !permittedUser -> event.message.reply("${CustomEmote.XMARK} You don't have the required permissions to do that! (${requiredPermissionsUser.joinToString { prettyPrintPermissionName(it) }})")
            (creatorOnly && !event.author.isCreator) -> event.message.addReaction("❓").queue() //Pretend the skill does not exist
            else -> this.onTrigger(event, ai)
        }
    }

    private fun prettyPrintPermissionName(permission: Permission) : String {
        return permission.name.split("_").joinToString(" ") { it.toLowerCase().capitalize() }
    }

    override fun toString(): String = trigger
}