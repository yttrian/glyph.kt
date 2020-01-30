/*
 * UserInfoSkill.kt
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

package me.ianmooreis.glyph.skills.moderation

import me.ianmooreis.glyph.directors.messaging.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.findUser
import me.ianmooreis.glyph.extensions.getInfoEmbed
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * A skill that allows users to get an info embed about other or themselves
 */
object UserInfoSkill : Skill("skill.moderation.userInfo") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val userName: String? = ai.result.getStringParameter("user")
        val user: User? = if (event.channelType.isGuild && userName != null) {
            event.guild.findUser(userName) ?: event.author
        } else {
            event.author
        }
        if (user == null) {
            event.message.reply("Unable to find the specified user!")
            return
        }
        event.message.reply(user.getInfoEmbed("User Info", "Moderation", null, showExactCreationDate = true, mutualGuilds = false))
    }
}