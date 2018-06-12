package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.MessagingOrchestrator
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.*

object StatusSkill : SkillAdapter("skill.status", cooldownTime = 5) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val jda = event.jda
        val name = jda.selfUser.name
        val discordDescription = SimpleDescriptionBuilder()
                .addField("Ping", "${jda.ping} ms")
                .addField("Guilds", jda.guilds.count())
                .addField("Shard", "${jda.shardInfo.shardId}${if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""}")
                .addField("Users", jda.users.size)
        if (event.author.isCreator) {
            discordDescription.addField("Messages", MessagingOrchestrator.getTotalMessages())
        }
        val embed = EmbedBuilder()
                .setTitle("$name Status")
                .addField("Discord", discordDescription.build(), true)
                .setFooter("$name-Kotlin-${Glyph.version}", null)
                .setTimestamp(Instant.now())
        if (event.author.isCreator) {
            val runtime = Runtime.getRuntime()
            val usedMemory = "%.2f".format((runtime.totalMemory() - runtime.freeMemory()).toFloat()/1000000)
            val maxMemory = "%.2f".format(runtime.maxMemory().toFloat()/1000000)
            val uptime = PrettyTime().format(Date(ManagementFactory.getRuntimeMXBean().startTime))
            val dynoDescription = SimpleDescriptionBuilder()
                    .addField("Cores", runtime.availableProcessors())
                    .addField("Memory", "$usedMemory of $maxMemory MB")
                    .addField("JVM", Runtime.version().toString())
                    .addField("Kotlin", KotlinVersion.CURRENT.toString())
                    .addField("JDA", JDAInfo.VERSION)
                    .addField("Restarted", uptime)
                    .build()
            embed.addField("Dyno", dynoDescription, true)
        } else {
            embed.setThumbnail(jda.selfUser.avatarUrl)
        }
        embed.addField("Operating Parameters", ai.result.fulfillment.speech.replace("\\n", "\n", true), true)
        event.message.reply(embed = embed.build())
    }
}