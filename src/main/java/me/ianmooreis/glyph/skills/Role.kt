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

object RoleSetSkill : Skill("skill.role.set", serverOnly = true, requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        if (!event.message.channelType.isGuild) {
            event.message.reply("You must be in a server to set your role!")
            return
        }
        if ((event.message.mentionedMembers.size > 0 || event.message.mentionsEveryone()) && !event.member.hasPermission(listOf(Permission.MANAGE_ROLES))) {
            event.message.reply("You must have Manage Roles permission to set other peoples' roles!")
        }
        val targets: List<Member> = when {
            event.message.mentionsEveryone() -> event.guild.members
            //@here -> event.guild.members.filter { it.onlineStatus == OnlineStatus.ONLINE }
            event.message.cleanMentionedMembers.isNotEmpty() -> event.message.cleanMentionedMembers
            else -> listOf(event.member)
        }
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        val selectableRoles = event.guild.config.selectableRoles.map { event.guild.getRolesByName(it, true).firstOrNull() }
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
                    event.guild.controller.addSingleRoleToMember(target, desiredRole).reason("Asked to be $desiredRoleName").queue()
                }
                event.message.reply(EmbedBuilder()
                        .setTitle("Poof!")
                        .setDescription(
                                "${targets.filter { it.roles.contains(desiredRole) }.joinToString { it.effectiveName }} " +
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
        event.message.reply(EmbedBuilder()
                .setTitle("Available Roles")
                .setDescription(event.guild.config.selectableRoles.map {
                    event.guild.getRolesByName(it, true).firstOrNull()
                }.joinToString("\n") { "**${it?.name ?: "Deleted role"}** (${it?.guild?.getMembersWithRoles(it)?.size ?: "No"} members) " })
                .setFooter("Roles", null)
                .setTimestamp(Instant.now())
                .build())
    }
}