package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.MessagingOrchestrator
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.*

object InfoSkill : SkillAdapter("skill.status") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val jda = event.jda
        val name = jda.selfUser.name
        val uptime = PrettyTime().format(Date(ManagementFactory.getRuntimeMXBean().startTime))
        val embed = EmbedBuilder()
                .setTitle("$name Status")
                .addField("Discord Info","**Ping** ${jda.ping} ms\n**Servers** ${jda.guilds.size}" +
                        "\n**Shard** ${jda.shardInfo.shardId + 1}/${jda.shardInfo.shardTotal}\n**Members** ${jda.users.size}" +
                        "\n**Messages** ${MessagingOrchestrator.getLedgerSize()}\n**Restarted** $uptime", true)
                .addField("Operating Parameters", ai.result.fulfillment.speech.replace("\\n", "\n", true), true)
                //.addField("Developer Rambling", ai.result.fulfillment.speech, false)
                //.setThumbnail(jda.selfUser.avatarUrl)
                .setFooter("$name-Kotlin-${Glyph.version}", null)
                .setTimestamp(Instant.now())
                .build()
        event.message.reply(embed = embed)
    }
}