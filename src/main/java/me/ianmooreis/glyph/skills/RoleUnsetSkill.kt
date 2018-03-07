package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.time.Instant

object RoleUnsetSkill : Skill("skill.role.unset", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //Check if the user is allowed to remove roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member.hasPermission(listOf(Permission.MANAGE_ROLES))) {
            event.message.reply("You must have Manage Roles permission to remove other peoples' roles!")
            return
        }
        //Get the list of target(s) based on the mentions in the messages
        val targets: List<Member> = when {
            event.message.mentionsEveryone() -> event.guild.members
            //@here -> event.guild.members.filter { it.onlineStatus == OnlineStatus.ONLINE }
            event.message.cleanMentionedMembers.isNotEmpty() -> event.message.cleanMentionedMembers
            else -> listOf(event.member)
        }
        //Extract the desired role name and make a list of all available selectable roles
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        if (desiredRoleName.isEmpty()) {
            event.message.reply("I could not find a role name in you message!")
            return
        }
        val selectableRoles = event.guild.config.selectableRoles.roles.mapNotNull{ event.guild.getRolesByName(it, true).firstOrNull() }
        if (selectableRoles.isEmpty()) {
            event.message.reply("${CustomEmote.XMARK} There are no selectable roles configured for this server!")
        }
        //If the desired role exists and is in the list of the selectable role
        if (desiredRole != null && selectableRoles.contains(desiredRole)) {
            //Remove the role
            targets.forEach {
                try {
                    event.guild.controller.removeRolesFromMember(it, desiredRole).reason("Asked to not be $desiredRoleName").queue()
                } catch (e: HierarchyException) {
                    this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                }
            }
            val targetNames = targets.joinToString { it.effectiveName }
            event.message.reply(EmbedBuilder()
                    .setTitle("Zap!")
                    .setDescription(
                            "${if (targetNames.length < 200) targetNames else "${targets.size} people"} " +
                                    "${if (targets.size == 1) "is" else "are"} no longer `${desiredRole.name}`!")
                    .setThumbnail(if (targets.size == 1) targets.first().user.avatarUrl else null)
                    .setFooter("Roles", null)
                    .setTimestamp(Instant.now())
                    .build())
        } else {
            event.message.reply("Sorry, you can not remove `$desiredRoleName`!")
        }
    }
}