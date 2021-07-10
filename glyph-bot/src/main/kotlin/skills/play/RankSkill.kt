/*
 * RankSkill.kt
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
 */

package org.yttr.glyph.bot.skills.play

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.asPlainMention
import org.yttr.glyph.bot.extensions.toDate
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.time.Instant

/**
 * A skill that allows members to see bragging rights such as join order or account age
 */
class RankSkill : Skill("skill.rank", guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        if (event.guild.memberCount > GUILD_SIZE_LIMIT) {
            return Response.Volatile(
                "Sorry, due to time constraints " +
                        "I will not attempt to rank servers with more than $GUILD_SIZE_LIMIT members!"
            )
        }

        val property: String? = ai.result.getStringParameter("memberProperty")

        return if (property != null) {
            event.guild.retrieveMembers().await()
            val members = event.guild.members
            when (property) {
                "join" -> Response.Volatile(rankMembersByJoin(members, event.member ?: event.guild.selfMember))
                "created" -> Response.Volatile(
                    rankMembersByCreation(members, event.member ?: event.guild.selfMember)
                )
                else -> Response.Volatile("I'm not sure what property `$property` is for members.")
            }
        } else {
            Response.Volatile("I'm not sure what the property you want to rank members by is.")
        }
    }

    private fun rankMembersByJoin(members: List<Member>, requester: Member): MessageEmbed {
        val rankedMembers = members.sortedBy { it.timeJoined }
        return createRankEmbed("Guild Join Rankings", rankedMembers, requester) {
            val prettyTime = PrettyTime().format(it.timeJoined.toDate())
            "**${it.asPlainMention}** joined **$prettyTime** on **${it.timeJoined}**"
        }
    }

    private fun rankMembersByCreation(members: List<Member>, requester: Member): MessageEmbed {
        val rankedMembers = members.sortedBy { it.user.idLong }
        return createRankEmbed("Account Creation Rankings", rankedMembers, requester) {
            val prettyTime = PrettyTime().format(it.user.timeCreated.toDate())
            "**${it.asPlainMention}** was created **$prettyTime** on **${it.user.timeCreated}**"
        }
    }

    private fun createRankEmbed(
        title: String,
        rankedMembers: List<Member>,
        requester: Member,
        description: (Member) -> String
    ): MessageEmbed {
        val notable = SimpleDescriptionBuilder()
        val notableMembers = ArrayList<Member>()
        notableMembers.addAll(rankedMembers.take(NOTABLE_GROUP_SIZE))
        notableMembers.addAll(rankedMembers.takeLast(NOTABLE_GROUP_SIZE))
        // TODO: Reduce big-O time complexity
        notableMembers.forEach {
            notable.addField("`${rankedMembers.indexOf(it).plus(1)}.`", description(it))
        }
        val requesterRankDescription = "`${rankedMembers.indexOf(requester).plus(1)}.` ${description(requester)}"
        val embed = EmbedBuilder()
            .setTitle(title)
            .addField("Notable", notable.build(), false)
            .addField("You", requesterRankDescription, true)
            .setFooter("Rank", null)
            .setTimestamp(Instant.now())
        return embed.build()
    }

    companion object {
        /**
         * The maximum size a guild can be for ranking to be allowed
         */
        const val GUILD_SIZE_LIMIT: Int = 50

        /**
         * The size of the notable groups
         */
        const val NOTABLE_GROUP_SIZE: Int = 3
    }
}
