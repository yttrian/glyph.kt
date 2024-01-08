package org.yttr.glyph.bot.skills.util

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.isCreator
import org.yttr.glyph.bot.messaging.MessagingDirector
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import org.yttr.glyph.shared.pubsub.redis.RedisAsync
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.Date

/**
 * A skill that shows users the current status of the client, with extra info for the creator only
 */
class StatusSkill(
    /**
     * Redis async connection
     */
    private val redis: RedisAsync
) : Skill("skill.status", cooldownTime = 5) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val jda = event.jda
        val name = jda.selfUser.name
        val discordDescription = SimpleDescriptionBuilder()
            .addField("Ping", "${jda.gatewayPing} ms")
            .addField("Guilds", jda.guilds.size)
            .addField(
                "Shard",
                "${jda.shardInfo.shardId}" + if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""
            )
        if (event.author.isCreator) {
            val messageCount = redis.get(MessagingDirector.MESSAGE_COUNT_KEY).await()
            discordDescription.addField("Messages", messageCount ?: "?")
        }
        val embed = EmbedBuilder()
            .setTitle("$name Status")
            .addField("Discord", discordDescription.build(), true)
            .setFooter("$name-Kotlin-${Glyph.version}", null)
            .setTimestamp(Instant.now())
        if (event.author.isCreator) {
            val runtime = Runtime.getRuntime()
            val usedMemory = "%.2f".format((runtime.totalMemory() - runtime.freeMemory()).toFloat() / 1000000)
            val maxMemory = "%.2f".format(runtime.maxMemory().toFloat() / 1000000)
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
        embed.addField("Operating Parameters", ai.result.fulfillment.speech, true)

        return Response.Volatile(embed.build())
    }
}
