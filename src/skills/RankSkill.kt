package org.yttr.glyph.skills

import dev.kord.core.event.message.MessageCreateEvent
import org.yttr.glyph.SimpleDescriptionBuilder
import org.yttr.glyph.ai.AIResponse
import java.time.Instant

/**
 * A skill that allows members to see bragging rights such as join order or account age
 */
object RankSkill : Skill("skill.rank", guildOnly = true) {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
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
