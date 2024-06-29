package org.yttr.glyph.presentation

import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.yttr.glyph.Director
import org.yttr.glyph.SimpleDescriptionBuilder
import org.yttr.glyph.quickviews.GREEN
import org.yttr.glyph.quickviews.RED

/**
 * Manages server related events
 */
object ServerDirector : Director {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun register(kord: Kord) {
        kord.on<GuildCreateEvent> {
            webhookLog(guild.buildEmbed("Guild Joined", Color.GREEN))
            log.info("Joined $guild")
        }

        kord.on<GuildDeleteEvent> {
            guild?.buildEmbed("Guild Left", Color.RED)?.let { webhookLog(it) }
            log.info("Left $guild")
        }
    }

    private fun webhookLog(embedBuilder: EmbedBuilder) {
        TODO()
    }

    private fun Guild.buildEmbed(title: String, color: Color) = EmbedBuilder().apply {
        this.title = title
        this.color = color

        description = SimpleDescriptionBuilder {
            addField(name = "Name", content = name)
            addField(name ="ID", content = id.toString())
            addField(name = "Members", content = memberCount?.toString() ?: "?")
        }

        val iconUrl = icon?.cdnUrl?.toUrl()

        if (iconUrl != null) {
            thumbnail {
                url = iconUrl
            }
        }

        footer {
            text = "Loggings"
        }

        timestamp = Clock.System.now()
    }
}
