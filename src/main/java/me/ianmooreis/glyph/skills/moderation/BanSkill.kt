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

object BanSkill : Skill("skill.moderation.ban", serverOnly = true, requiredPermissionsSelf = listOf(Permission.BAN_MEMBERS), requiredPermissionsUser = listOf(Permission.BAN_MEMBERS)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        KickBanSkillHelper.getInstance(event, ai, "ban") { targets, reason, controller ->
            event.message.delete().reason("Ban request").queue()
            targets.forEach { member ->
                if (!member.user.isBot) {
                    member.user.openPrivateChannel().queue { pm ->
                        pm.sendMessage("***${CustomEmote.GRIMACE} You have been banned from ${event.guild.name} for \"$reason\"!***").queue {
                            pm.close().queue {
                                controller.ban(member, 7, reason).queue()
                            }
                        }
                    }
                }

            }
            val targetNames = targets.joinToString { it.asPlainMention }
            event.message.reply("${CustomEmote.CHECKMARK} " +
                    "***${if (targetNames.length < 200) targetNames else "${targets.size} people"} ${if (targets.size == 1) "has" else "have"} been banned!***")
            if (event.guild.config.auditing.bans) {
                event.guild.audit("Members Banned",
                        "**Who** ${if (targetNames.length < 200) targetNames else "${targets.size} people"}\n" +
                                "**Reason** $reason\n" +
                                "**Blame** ${event.author.asMention}")
            }
        }
    }
}