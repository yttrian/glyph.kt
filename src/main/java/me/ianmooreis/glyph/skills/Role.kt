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
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object RoleSetSkill : Skill("skill.role.set", serverOnly = true, requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        if (!event.message.channelType.isGuild) {
            event.message.reply("You must be in a server to set your role!")
            return
        }
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member.hasPermission(listOf(Permission.MANAGE_ROLES))) {
            event.message.reply("You must have Manage Roles permission to set other peoples' roles!")
            return
        }
        val targets: List<Member> = when {
            event.message.mentionsEveryone() -> event.guild.members
            //@here -> event.guild.members.filter { it.onlineStatus == OnlineStatus.ONLINE }
            event.message.cleanMentionedMembers.isNotEmpty() -> event.message.cleanMentionedMembers
            else -> listOf(event.member)
        }
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        val selectableRoles = event.guild.config.selectableRoles.mapNotNull{ event.guild.getRolesByName(it, true).firstOrNull() }
        if (selectableRoles.isEmpty()) {
            event.message.reply("${CustomEmote.XMARK} There are no selectable roles configured for this server!")
        }
        if (desiredRole != null && selectableRoles.contains(desiredRole)) {
            targets.forEach {
                try {
                    event.guild.controller.removeRolesFromMember(it, selectableRoles).reason("Asked to be $desiredRoleName").queue()
                } catch (e: IllegalArgumentException) {
                    this.log.debug("No roles needed to be removed from $it in ${event.guild}")
                } catch (e: HierarchyException) {
                    this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                }
            }
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
            } catch (e:HierarchyException) {
                event.message.reply("${CustomEmote.XMARK} I can not set anyone as `${desiredRole.name}` because it is above my highest role!")
                return
            }
        } else {
            event.message.reply("Sorry, you can not be a `$desiredRoleName`!")
        }
    }
}

object RoleListSkill : Skill("skill.role.list", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val roles = event.guild.config.selectableRoles.mapNotNull { event.guild.getRolesByName(it, true).firstOrNull() }
        event.message.reply(EmbedBuilder()
                .setTitle("Available Roles")
                .setDescription(roles.joinToString("\n") {
                    val size = it.guild.getMembersWithRoles(it).size
                    "**${it.name}** ($size ${if (size == 1) "member" else "members"})"
                })
                .setFooter("Roles | Try asking \"Set me as ${getRandomRole(roles).name}\"", null)
                .setTimestamp(Instant.now())
                .build())
    }

    private fun getRandomRole(roles: List<Role>): Role {
        return roles[Random().nextInt(roles.size)]
    }
}