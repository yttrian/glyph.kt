package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.getRandom
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

object RoleListSkill : Skill("skill.role.list", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val selectableRoles = event.guild.config.selectableRoles.roles.filterNotNull().filter { it != "" }
        val limit = event.guild.config.selectableRoles.limit
        if (selectableRoles.isNotEmpty()) {
            val roles = selectableRoles.mapNotNull { event.guild.getRolesByName(it, true).firstOrNull() }
            event.message.reply(EmbedBuilder()
                    .setTitle("Available Roles")
                    .setDescription(roles.joinToString("\n") {
                        val size = it.guild.getMembersWithRoles(it).size
                        "**${it.name}** ($size ${if (size == 1) "member" else "members"})"
                    } + if (limit > 0) "\n*You can have up to $limit ${if (limit == 1) "role" else "roles"}*" else "")
                    .setFooter("Roles | Try asking \"Set me as ${roles.getRandom().name}\"", null)
                    .setTimestamp(Instant.now())
                    .build())
        } else {
            event.message.reply("There are no selectable roles configured!")
        }
    }
}