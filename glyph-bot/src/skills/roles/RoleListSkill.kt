package org.yttr.glyph.bot.skills.roles

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.time.Instant

/**
 * A skill that allows members to list all selectable roles
 */
class RoleListSkill : Skill("skill.role.list", guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val selectableRoles = event.guild.config.selectableRoles.roles.mapNotNull { event.guild.getRoleById(it) }
        val limit = event.guild.config.selectableRoles.limit

        return if (selectableRoles.isNotEmpty()) {
            val randomRole = selectableRoles.random()
            val description = StringBuilder()

            selectableRoles.forEach {
                description.appendLine(it.asMention)
            }
            if (limit > 0) description.append("*You can have up to $limit ${if (limit == 1) "role" else "roles"}*")

            Response.Volatile(
                EmbedBuilder()
                    .setTitle("Available Roles")
                    .setDescription(description)
                    .setFooter("Roles | Try asking \"Set me as ${randomRole.name}\"")
                    .setTimestamp(Instant.now())
                    .build()
            )
        } else {
            Response.Volatile("There are no selectable roles configured!")
        }
    }
}
