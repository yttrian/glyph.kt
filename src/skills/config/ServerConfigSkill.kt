package org.yttr.glyph.skills.config

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.messaging.Response
import org.yttr.glyph.skills.Skill

/**
 * Tells people to use the config website to edit their config
 */
class ServerConfigSkill : Skill(
    "skill.config.server",
    cooldownTime = 15,
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.MANAGE_WEBHOOKS),
    requiredPermissionsUser = listOf(Permission.MANAGE_SERVER)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response =
        Response.Volatile("To edit your config, visit https://gl.yttr.org/config")
}
