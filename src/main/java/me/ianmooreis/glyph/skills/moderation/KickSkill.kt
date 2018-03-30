package me.ianmooreis.glyph.skills.moderation

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.audit
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object KickSkill : Skill("skill.moderation.kick", serverOnly = true, requiredPermissionsSelf = listOf(Permission.KICK_MEMBERS), requiredPermissionsUser = listOf(Permission.KICK_MEMBERS)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        KickBanSkillHelper.getInstance(event, ai, "kick") { targets, reason, controller ->
            event.message.delete().reason("Kick request").queue()
            targets.forEach { member ->
                val finally = { controller.kick(member, reason).queue() }
                if (!member.user.isBot) {
                    member.user.openPrivateChannel().queue { pm ->
                        pm.sendMessage("***${CustomEmote.GRIMACE} You have been kicked from ${event.guild.name} for \"$reason\"!***").queue({
                            pm.close().queue { finally() }
                        }, { finally() })
                    }
                } else {
                    finally()
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            event.message.reply("${CustomEmote.CHECKMARK} " +
                    "***${if (targetNames.length < 200) targetNames else "${targets.size} people"} ${if (targets.size == 1) "was" else "were"} kicked!***")
            if (event.guild.config.auditing.kicks) {
                event.guild.audit("Members Kicked",
                        "**Who** ${if (targetNames.length < 200) targetNames else "${targets.size} people"}\n" +
                                "**Reason** $reason\n" +
                                "**Blame** ${event.author.asMention}")
            }
        }
    }
}