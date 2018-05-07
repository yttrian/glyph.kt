package me.ianmooreis.glyph.skills.roles

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.time.Instant

object RoleUnsetSkill : SkillAdapter("skill.role.unset", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //Check if the user is allowed to remove roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member.hasPermission(listOf(Permission.MANAGE_ROLES))) {
            event.message.reply("You must have Manage Roles permission to remove other peoples' roles!")
            return
        }
        RoleSkillHelper.getInstance(event, ai) { desiredRole, _, targets ->
            //Remove the role
            targets.forEach {
                try {
                    event.guild.controller.removeRolesFromMember(it, desiredRole).reason("Asked to not be $desiredRole.Name").queue()
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
        }
    }
}