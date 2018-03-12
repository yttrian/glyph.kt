package me.ianmooreis.glyph.skills.roles

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.getRandom
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
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
        RoleSkillHelper.getInstance(event, ai) { desiredRole, selectableRoles, targets ->
            val config = event.guild.config.selectableRoles
            //If the user is the only target and does not have manage roles permission and would violate the limit, make them remove a role first (mods can ignore this)
            if (targets.size > 1 && targets.contains(event.member) && !event.member.hasPermission(Permission.MANAGE_ROLES)
                    && event.member.roles.count { selectableRoles.contains(it) } >= config.limit) {
                event.message.reply("" +
                        "${CustomEmote.XMARK} You can only have ${config.limit} roles in this server! " +
                        "Try removing one first, by telling me for example: \"remove me from ${event.member.roles.filter { selectableRoles.contains(it) }.getRandom().name}\"")
            } else  {
                //Remove old roles if the sever role limit is 1, this is the default and is meant for switching roles
                if (config.limit == 1) {
                    targets.forEach {
                        try {
                            event.guild.controller.removeRolesFromMember(it, selectableRoles).reason("Asked to be ${desiredRole.name}").queue()
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
                        event.guild.controller.addSingleRoleToMember(target, desiredRole).reason("Asked to be ${desiredRole.name}").queueAfter(500, TimeUnit.MILLISECONDS)
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
                }
            }
        }
    }
}