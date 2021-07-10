/*
 * ServerConfigSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */kage org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill

/**
 * Tells people to use the config website to edit their config
 */
class ServerConfigSkill : Skill(
    "skill.config.server",
    cooldownTime = 15,
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.MANAGE_WEBHOOKS),
    requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response =
        Response.Volatile("To edit your config, visit https://gl.yttr.org/config")
}
