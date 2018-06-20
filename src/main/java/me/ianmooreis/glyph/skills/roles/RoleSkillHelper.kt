package me.ianmooreis.glyph.skills.roles

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

/**
 * Helps find a selectable role a member is asking about
 */
object RoleSkillHelper {
    /**
     * Attempt to figure out what selectable role a member is asking about or tell them what went wrong
     *
     * @param event the message event
     * @param ai the ai response
     * @param success the callback to run if the desired role exists and is found
     */
    fun getInstance(event: MessageReceivedEvent, ai: AIResponse, success: (desiredRole: Role, selectableRoles: List<Role>, targets: List<Member>) -> Unit) {
        //Get the list of target(s) based on the mentions in the messages
        val targets: List<Member> = when {
            event.message.mentionsEveryone() -> event.guild.members
        //@here -> event.guild.members.filter { it.onlineStatus == OnlineStatus.ONLINE }
            event.message.cleanMentionedMembers.isNotEmpty() -> event.message.cleanMentionedMembers
            else -> listOf(event.member)
        }
        //Extract the desired role name and make a list of all available selectable roles
        val config = event.guild.config.selectableRoles
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        if (desiredRoleName.isEmpty()) {
            event.message.reply("I could not find a role name in your message!")
            return
        }
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        if (desiredRole == null) {
            event.message.reply("That role does not exist!")
            return
        }
        val selectableRoles = config.roles.mapNotNull { event.guild.getRolesByName(it, true).firstOrNull() }
        if (selectableRoles.isEmpty()) {
            event.message.reply("${CustomEmote.XMARK} There are no selectable roles configured for this server!")
            return
        }
        if (selectableRoles.contains(desiredRole)) {
            success(desiredRole, selectableRoles, targets)
        } else {
            event.message.reply("Sorry, `$desiredRoleName` is not a selectable role!")
        }
    }
}