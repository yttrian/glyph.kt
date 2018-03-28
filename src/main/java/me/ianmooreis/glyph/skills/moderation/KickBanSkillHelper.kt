package me.ianmooreis.glyph.skills.moderation

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.managers.GuildController

object KickBanSkillHelper {
    fun getInstance(event: MessageReceivedEvent, ai: AIResponse, action: String, success: (targets: List<Member>, reason: String, controller: GuildController) -> Unit) {
        val authorMaxRole = maxRolePosition(event.member)
        val selfMaxRole = maxRolePosition(event.guild.selfMember)
        val targets = event.message.cleanMentionedMembers
        when {
            event.message.mentionsEveryone() -> event.message.reply("You cannot $action everyone at once!")
            targets.isEmpty() -> event.message.reply("You need to @mention at least one member to $action!")
            targets.contains(event.member) -> event.message.reply("You cannot $action yourself!")
            targets.contains(event.guild.owner) -> event.message.reply("You cannot $action the owner!")
            targets.filterNot { it.hasPermission(Permission.ADMINISTRATOR) }.isEmpty() -> event.message.reply("I will not $action someone with Administrator permissions!")
            targets.filterNot { it.hasPermission(Permission.MANAGE_SERVER) }.isEmpty() -> event.message.reply("I will not $action someone with Manage Server permissions!")
            targets.contains(event.guild.selfMember) -> event.message.reply("I cannot kick myself!")
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= authorMaxRole }.isEmpty() && !event.member.isOwner -> event.message.reply("You cannot $action members of your role or higher!")
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= selfMaxRole }.isEmpty() -> event.message.reply("I cannot $action members of my role or higher!")
            else -> success(targets, ai.result.getStringParameter("reason", "No reason provided"), event.guild.controller)
        }
    }

    private fun maxRolePosition(member: Member): Int {
        return member.roles.map { it.position }.max() ?: 0
    }
}