package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.MessageOrchestrator
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

object InfoSkill : Skill("skill.status") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val jda = event.jda
        val name = jda.selfUser.name
        val embed = EmbedBuilder()
                .setTitle("$name Status")
                .addField("Discord Info","**Ping** ${jda.ping} ms\n**Servers** ${jda.guilds.size}" +
                        "\n**Shard** ${jda.shardInfo.shardId + 1}/${jda.shardInfo.shardTotal}\n**Members** ${jda.users.size}" +
                        "\n**Messages** ${MessageOrchestrator.getLedgerSize()}", true)
                .addField("Operating Parameters", "Must not misrepresent reality.\nMust remain compatible with objective truth.\nMust be obedient.", true)
                //.setThumbnail(jda.selfUser.avatarUrl)
                .setFooter("$name-Kotlin-${System.getenv("HEROKU_RELEASE_VERSION")}", null)
                .setTimestamp(Instant.now())
                .build()
        event.message.reply(embed = embed)
    }
}