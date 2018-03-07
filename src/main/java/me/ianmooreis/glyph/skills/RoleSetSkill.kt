package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.getRandom
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
import java.util.concurrent.TimeUnit

object RoleSetSkill : Skill("skill.role.set", serverOnly = true, requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //Check if the user is allowed to set roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member.hasPermission(Permission.MANAGE_ROLES)) {
            event.message.reply("You must have Manage Roles permission to set other peoples' roles!")
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
        val config = event.guild.config.selectableRoles
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        if (desiredRoleName.isEmpty()) {
            event.message.reply("I could not find a role name in you message!")
            return
        }
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        val selectableRoles = config.roles.mapNotNull{ event.guild.getRolesByName(it, true).firstOrNull() }
        if (selectableRoles.isEmpty()) {
            event.message.reply("${CustomEmote.XMARK} There are no selectable roles configured for this server!")
        }
        //If the user is the only target and does not have manage roles permission and would violate the limit, make them remove a role first (mods can ignore this)
        if (targets.size == 1 && targets.contains(event.member) && !event.member.hasPermission(Permission.MANAGE_ROLES)
                && event.member.roles.count { selectableRoles.contains(it) } >= config.limit) {
            event.message.reply("" +
                    "${CustomEmote.XMARK} You can only have ${config.limit} roles in this server! " +
                    "Try removing one first, by telling me for example: \"remove me from ${event.member.roles.filter { selectableRoles.contains(it) }.getRandom().name}\"")
            return
        }
        //If the desired role exists and is in the list of the selectable roles
        if (desiredRole != null && selectableRoles.contains(desiredRole)) {
            //Remove old roles if the sever role limit is 1, this is the default and is meant for switching roles
            if (config.limit == 1) {
                targets.forEach {
                    try {
                        event.guild.controller.removeRolesFromMember(it, selectableRoles).reason("Asked to be $desiredRoleName").queue()
                    } catch (e: IllegalArgumentException) {
                        this.log.debug("No roles needed to be removed from $it in ${event.guild}")
                    } catch (e: HierarchyException) {
                        this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                    }
                }
            }
            //Grant everyone the desired role but report a warning if the role is too high
            try {
                targets.forEach { target ->
                    event.guild.controller.addSingleRoleToMember(target, desiredRole).reason("Asked to be $desiredRoleName").queueAfter(500, TimeUnit.MILLISECONDS)
                }
                val targetNames = targets.joinToString { it.effectiveName }
                event.message.reply(EmbedBuilder()
                        .setTitle("Poof!")
                        .setDescription(
                                "${if (targetNames.length < 200) targetNames else "${targets.size} people"} " +
                                        "${if (targets.size == 1) "is" else "are"} now `${desiredRole.name}`!")
                        .setThumbnail(if (targets.size == 1) targets.first().user.avatarUrl else null)
                        .setFooter("Roles", null)
                        .setTimestamp(Instant.now())
                        .build())
            } catch (e: HierarchyException) {
                event.message.reply("${CustomEmote.XMARK} I can not set anyone as `${desiredRole.name}` because it is above my highest role!")
                return
            }
        } else {
            event.message.reply("Sorry, you can not be a `$desiredRoleName`!")
        }
    }
}