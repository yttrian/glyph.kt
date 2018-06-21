package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.extensions.toDate
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.collections4.ListUtils
import org.ocpsoft.prettytime.PrettyTime
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

    private fun rankMembersByJoin(members: List<Member>, requester: Member): MessageEmbed {
        val rankedMembers = members.sortedBy { it.joinDate }
        return createRankEmbed("Guild Join Rankings", rankedMembers, requester) {
            "**${it.asPlainMention}** joined **${PrettyTime().format(it.joinDate.toDate())}** on **${it.joinDate}**"
        }
    }

    private fun rankMembersByCreation(members: List<Member>, requester: Member): MessageEmbed {
        val rankedMembers = members.sortedBy { it.user.idLong }
        return createRankEmbed("Account Creation Rankings", rankedMembers, requester) {
            "**${it.asPlainMention}** was created **${PrettyTime().format(it.user.creationTime.toDate())}** on **${it.user.creationTime}**"
        }
    }

    private fun createRankEmbed(title: String, rankedMembers: List<Member>, requester: Member, description: (Member) -> String): MessageEmbed {
        val notable = SimpleDescriptionBuilder()
        ListUtils.union(rankedMembers.take(3), rankedMembers.takeLast(3)).forEach {
            notable.addField("`${rankedMembers.indexOf(it).plus(1)}.`", description(it))
        }
        val requesterRankDescription = "`${rankedMembers.indexOf(requester).plus(1)}.` ${description(requester)}"
        return EmbedBuilder()
            .setTitle(title)
            .addField("Notable", notable.build(), true)
            .addField("You", requesterRankDescription, false)
            .setFooter("Rank", null)
            .setTimestamp(Instant.now())
            .build()
    }
}