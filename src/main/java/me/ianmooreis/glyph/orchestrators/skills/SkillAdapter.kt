package me.ianmooreis.glyph.orchestrators.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.util.concurrent.TimeUnit

abstract class SkillAdapter(val trigger: String, private val cooldownTime: Long = 2, private val cooldownUnit: TimeUnit = TimeUnit.SECONDS,
                            private val guildOnly: Boolean = false,
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
        val currentCooldown: SkillCooldown? = SkillOrchestrator.getCooldown(event.author, this)
        when {
            currentCooldown != null && !currentCooldown.expired ->
                if (!currentCooldown.warned) {
                    event.message.reply("⏲ `$trigger` is on cooldown, please wait ${currentCooldown.remainingSeconds} seconds.", deleteAfterDelay = 5)
                    currentCooldown.warned = true
                } else {
                    event.message.addReaction("⏲").queue() //React with :timer: to indicate cooldown
                }
            (guildOnly || requiredPermissionsUser.isNotEmpty()) && !event.channelType.isGuild -> event.message.reply("${CustomEmote.XMARK} You can only do that in a server!")
            !permittedSelf -> event.message.reply("${CustomEmote.XMARK} I don't have the required permissions to do that! (${requiredPermissionsSelf.joinToString { prettyPrintPermissionName(it) }})")
            !permittedUser -> event.message.reply("${CustomEmote.XMARK} You don't have the required permissions to do that! (${requiredPermissionsUser.joinToString { prettyPrintPermissionName(it) }})")
            creatorOnly && !event.author.isCreator -> event.message.addReaction("❓").queue() //Pretend the skill does not exist
            else -> {
                this.onTrigger(event, ai)
                SkillOrchestrator.setCooldown(event.author, this, SkillCooldown(cooldownTime, cooldownUnit))
            }
        }
    }

    private fun prettyPrintPermissionName(permission: Permission) : String {
        return permission.name.split("_").joinToString(" ") { it.toLowerCase().capitalize() }
    }

    override fun toString(): String = trigger
}