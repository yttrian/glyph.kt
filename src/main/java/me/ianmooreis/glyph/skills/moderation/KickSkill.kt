package me.ianmooreis.glyph.skills.moderation

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.*
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant
import java.util.concurrent.TimeUnit

object KickSkill : Skill("skill.moderation.kick", serverOnly = true, requiredPermissionsSelf = listOf(Permission.KICK_MEMBERS), requiredPermissionsUser = listOf(Permission.KICK_MEMBERS)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val authorMaxRole = maxRolePosition(event.member)
        val selfMaxRole = maxRolePosition(event.guild.selfMember)
        val targets = event.message.cleanMentionedMembers
        when {
            event.message.mentionsEveryone() -> event.message.reply("You cannot kick everyone at once!")
            targets.isEmpty() -> event.message.reply("You need to @mention at least one member to kick!")
            targets.contains(event.member) -> event.message.reply("You cannot kick yourself!")
            targets.contains(event.guild.owner) -> event.message.reply("You cannot kick the owner!")
            targets.filterNot { it.hasPermission(Permission.ADMINISTRATOR) }.isEmpty() -> event.message.reply("I will not kick someone with Administrator permissions!")
            targets.filterNot { it.hasPermission(Permission.MANAGE_SERVER) }.isEmpty() -> event.message.reply("I will not kick someone with Manage Server permissions!")
            targets.contains(event.guild.selfMember) -> event.message.reply("I cannot kick myself!")
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= authorMaxRole }.isEmpty() && !event.member.isOwner -> event.message.reply("You cannot kick members of your role or higher!")
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= selfMaxRole }.isEmpty() -> event.message.reply("I cannot kick members of my role or higher!")
            else -> {
                event.message.delete().reason("Kick request").queue()
                val reason = ai.result.getStringParameter("reason", "No reason provided")
                val controller = event.guild.controller
                targets.forEach {
                    if (!it.user.isBot) {
                        it.user.openPrivateChannel().queue {
                            it.sendMessage("***${CustomEmote.GRIMACE} You have been kicked from ${event.guild.name} for \"$reason\"!***").queue()
                            it.close().queue()
                        }
                    }
                    controller.kick(it, reason).queueAfter(500, TimeUnit.MILLISECONDS)
                }
                val targetNames = targets.joinToString { it.asPlainMention }
                event.message.reply(EmbedBuilder()
                        .setTitle("Kick")
                        .setDescription(
                                "${if (targetNames.length < 200) targetNames else "${targets.size} people"} " +
                                        "${if (targets.size == 1) "has" else "have"} been kicked!")
                        .addField("Reason", reason, false)
                        .setThumbnail(if (targets.size == 1) targets.first().user.avatarUrl else null)
                        .setFooter("Moderation", null)
                        .setTimestamp(Instant.now())
                        .build(), deleteWithEnabled = false)
                if (event.guild.config.auditing.kicks) {
                    event.guild.audit("Members Kicked",
                            "**Who** ${if (targetNames.length < 200) targetNames else "${targets.size} people"}\n" +
                                    "**Reason** $reason\n" +
                                    "**Blame** ${event.author.asMention}")
                }
            }
        }
    }

    private fun maxRolePosition(member: Member): Int {
        return member.roles.map { it.position }.max() ?: 0
    }
}