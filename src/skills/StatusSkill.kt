package org.yttr.glyph.skills

import dev.kord.core.entity.effectiveName
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.count
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.yttr.glyph.SimpleDescriptionBuilder
import org.yttr.glyph.ai.AIResponse
import java.lang.management.ManagementFactory

/**
 * A skill that shows users the current status of the client, with extra info for the creator only
 */
object StatusSkill : Skill("skill.status") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val kord = event.kord
        val self = kord.getSelf()
        val name = self.effectiveName
        val guildCount = kord.guilds.count()

        event.reply {
            embed {
                title = "$name Status"
                field(name = "Discord", inline = true) {
                    SimpleDescriptionBuilder {
                        addField(name = "Ping", content = "${kord.gateway.averagePing} ms")
                        addField(name = "Guilds", content = guildCount)
                        // TODO: Add message count
                    }
                }
                if (event.member?.isCreator == true) {
                    field(name = "Dyno", inline = true) { dynoDescription() }
                } else {
                    thumbnail {
                        url = self.defaultAvatar.cdnUrl.toUrl()
                    }
                }
                field(name = "Operating Parameters", inline = true) { ai.result.fulfillment.speech }
                footer {
                    text = "$name-Kotlin-${conf.getString("version")}"
                }
                timestamp = Clock.System.now()
            }
        }
    }

    private fun dynoDescription(): String = SimpleDescriptionBuilder {
        val runtime = Runtime.getRuntime()
        val usedMemory = "%.2f".format((runtime.totalMemory() - runtime.freeMemory()).toFloat() / 1000000)
        val maxMemory = "%.2f".format(runtime.maxMemory().toFloat() / 1000000)
        val uptime = Instant.fromEpochMilliseconds(ManagementFactory.getRuntimeMXBean().startTime)

        addField("Cores", runtime.availableProcessors())
        addField("Memory", "$usedMemory of $maxMemory MB")
        addField("JVM", Runtime.version().toString())
        addField("Kotlin", KotlinVersion.CURRENT.toString())
        addField("Restarted", uptime.toString())
    }
}
