package me.ianmooreis.glyph.skills

import club.minnced.kjda.promise
import humanize.Humanize
import me.ianmooreis.glyph.MessageOrchestrator
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.joda.time.DateTime
import java.awt.Color
import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalQueries

fun helpSkill(event: MessageReceivedEvent) {
    val embed = EmbedBuilder()
            .setTitle("Glyph Help")
            .setDescription("I am **Glyph**!\n\n" +
                    "A **constantly evolving** and **learning** Discord bot created by Throudin#4867.\n\n" +
                    "I use **machine learning** to process **natural language** requests you give to me to the best of my current trained ability.\n\n" +
                    "To see what I can do, be sure to check out my **full skills list** and **suggest new ones** you'd like to see, in the official server.\n\n" +
                    "[Full Skills List](https://glyph-discord.readthedocs.io/en/latest/skills.html) - " +
                    "[Official Glyph Server](https://discord.me/glyph-discord) - " +
                    "[Add Me To Your Server](http://glyph-discord.rtfd.io/invite)")
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()
    event.message.channel.sendMessage(embed).promise().then {
        MessageOrchestrator.amendLedger(event.messageId, it.id)
    }
}

fun statusSkill(event: MessageReceivedEvent) {
    val jda = event.jda
    val uptime = Humanize.duration(ManagementFactory.getRuntimeMXBean().uptime)
    val time: TemporalAccessor = Instant.now()
    val embed = EmbedBuilder()
            .setTitle("Glyph Status")
            .addField("Discord Info","**Ping** ${jda.ping} ms\n**Servers** ${jda.guilds.size}" +
                    "\n**Shard** ${jda.shardInfo.shardId + 1}/${jda.shardInfo.shardTotal}\n**Members** ${jda.users.size}" +
                    "\n**Messages** ${MessageOrchestrator.getLedgerSize()}", true)
            .setThumbnail(jda.selfUser.avatarUrl)
            .setFooter("Last restarted $uptime", null)
            .setTimestamp(time)
            .build()
    event.message.channel.sendMessage(embed).promise().then {
        MessageOrchestrator.amendLedger(event.messageId, it.id)
    }
}