package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.collections4.ListUtils
import java.time.Instant

/**
 * A skill that allows members to see bragging rights such as join order or account age
 */
object RankSkill : Skill("skill.rank", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val property: String? = ai.result.getStringParameter("memberProperty", null)
        if (property != null) {
            val members = event.guild.members
            when (property) {
                "join" -> event.message.reply(rankMembersByJoin(members, event.member))
                "created" -> event.message.reply(rankMembersByCreation(members, event.member))
                else -> event.message.reply("I'm not sure what property `$property` is for members.")
            }
        } else {
            event.message.reply("I'm not sure what the property you want to rank members by is.")
        }
    }

    private data class Ranking(val rank: Int, val description: String)

    private fun rankMembersByJoin(members: List<Member>, requester: Member): MessageEmbed {
        fun getRank(member: Member, ranking: List<Member>): Ranking {
            return Ranking(
                ranking.indexOf(member).plus(1),
                "${member.asMention} joined on **${member.joinDate}**")
        }

        val rankedMembers = members.sortedBy { it.joinDate }
        val notable = SimpleDescriptionBuilder()
        ListUtils.union(rankedMembers.take(3), rankedMembers.takeLast(3)).forEach {
            val rank = getRank(it, rankedMembers)
            notable.addField("`${rank.rank}.`", rank.description)
        }
        val requesterRank = getRank(requester, rankedMembers)
        val requesterRankDescription = SimpleDescriptionBuilder()
            .addField("`${requesterRank.rank}.`", requesterRank.description).build()
        return EmbedBuilder()
            .setTitle("Guild Join Rankings")
            .addField("Notable", notable.build(), true)
            .addField("You", requesterRankDescription, false)
            .setFooter("Rank", null)
            .setTimestamp(Instant.now())
            .build()
    }

    private fun rankMembersByCreation(members: List<Member>, requester: Member): MessageEmbed {
        fun getRank(member: Member, ranking: List<Member>): Ranking {
            return Ranking(
                ranking.indexOf(member).plus(1),
                "${member.asMention} was created on **${member.user.creationTime}**")
        }

        val rankedMembers = members.sortedBy { it.user.idLong }
        val notable = SimpleDescriptionBuilder()
        ListUtils.union(rankedMembers.take(3), rankedMembers.takeLast(3)).forEach {
            val rank = getRank(it, rankedMembers)
            notable.addField("`${rank.rank}.`", rank.description)
        }
        val requesterRank = getRank(requester, rankedMembers)
        val requesterRankDescription = SimpleDescriptionBuilder()
            .addField("`${requesterRank.rank}.`", requesterRank.description).build()
        return EmbedBuilder()
            .setTitle("Account Creation Rankings")
            .addField("Notable", notable.build(), true)
            .addField("You", requesterRankDescription, false)
            .setFooter("Rank", null)
            .setTimestamp(Instant.now())
            .build()
    }
}