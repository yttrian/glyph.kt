/*
 * FarmsSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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
 */

package me.ianmooreis.glyph.skills.creator

import ai.api.model.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.botRatio
import me.ianmooreis.glyph.extensions.isBotFarm
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

/**
 * A skill that allows the creator to check the status of bot farms and ask the client to leave them
 */
object FarmsSkill : Skill("skill.creator.farms", creatorOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val action = ai.result.getStringParameter("action", "list")
        val farms = event.jda.guilds.filter { it.isBotFarm }
        val farmsText = if (farms.isNotEmpty()) farms.joinToString("\n") { "${it.name} (${it.botRatio})" } else "No farms."
        event.channel.sendTyping().queue()
        when (action) {
            "list" -> event.message.reply("**Farms List**\n$farmsText")
            "leave" -> {
                event.message.reply("**Farms Left**\n$farmsText")
                farms.forEach { guild ->
                    guild.leave().queue {
                        log.info("Left bot farm $guild! Guild had bot ratio of ${guild.botRatio}")
                    }
                }
            }
        }
    }
}