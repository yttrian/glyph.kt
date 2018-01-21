package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.config
import me.ianmooreis.glyph.orchestrators.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant
import java.util.concurrent.TimeUnit

object RoleSetSkill : Skill("skill.role.set", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        if (!event.message.channelType.isGuild) {
            event.message.reply("You must be in a server to set your role!")
            return
        }
        val target = event.member //TODO: Allow choosing other people
        val desiredRoleName: String = ai.result.getStringParameter("role").removeSurrounding("\"")
        val desiredRole = event.guild.getRolesByName(desiredRoleName, true).firstOrNull()
        val selectableRoles = event.guild.config.selectable_roles.map { event.guild.getRolesByName(it, true).firstOrNull() } //TODO: Check for no selectable roles
        if (desiredRole != null && selectableRoles.contains(desiredRole)) {
            event.guild.controller.removeRolesFromMember(target, selectableRoles).reason("Asked to be a $desiredRoleName").queue()
            event.guild.controller.addSingleRoleToMember(target, desiredRole).reason("Asked to be a $desiredRoleName").queueAfter(2, TimeUnit.SECONDS)
            if (target.roles.contains(desiredRole)) {
                event.message.reply(embed = EmbedBuilder()
                        .setTitle("Poof!")
                        .setDescription("${target.effectiveName} you are now a ${desiredRole.name}!")
                        .setThumbnail(target.user.avatarUrl)
                        .setFooter("Roles", null)
                        .setTimestamp(Instant.now())
                        .build())
            } else {
                event.message.reply("Sorry, I was unable to set you as `${desiredRole.name}`")
            }
            return
        } else {
            event.message.reply("Sorry, you can not be a `$desiredRoleName`!")
        }
    }
}

object RoleListSkill : Skill("skill.role.list", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.message.reply(EmbedBuilder()
                .setTitle("Available Roles")
                .setDescription(event.guild.config.selectable_roles.map {
                    event.guild.getRolesByName(it, true).firstOrNull()
                }.joinToString("\n") { "**${it?.name ?: "Deleted role"}** (${it?.guild?.getMembersWithRoles(it)?.size ?: "No"} members) " })
                .setFooter("Roles", null)
                .setTimestamp(Instant.now())
                .build())
    }
}