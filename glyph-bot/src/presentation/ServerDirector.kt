package org.yttr.glyph.bot.presentation

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import org.yttr.glyph.bot.Director
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.log
import java.awt.Color
import java.time.Instant

/**
 * Manages server related events
 */
class ServerDirector : Director() {
    /**
     * When the client joins a guild
     */
    override fun onGuildJoin(event: GuildJoinEvent) {
        event.jda.selfUser.log(event.guild.descriptionEmbed.setTitle("Guild Joined").setColor(Color.GREEN).build())
        log.info("Joined ${event.guild}")
    }

    /**
     * When the client leaves a guild
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        event.jda.selfUser.log(event.guild.descriptionEmbed.setTitle("Guild Left").setColor(Color.RED).build())
        log.info("Left ${event.guild}")
    }

    private val Guild.descriptionEmbed: EmbedBuilder
        get() {
            val description = SimpleDescriptionBuilder()
                .addField("Name", this.name)
                .addField("ID", this.id)
                .addField("Members", this.memberCount)
                .build()
            return EmbedBuilder()
                .setDescription(description)
                .setThumbnail(this.iconUrl)
                .setFooter("Logging", null)
                .setTimestamp(Instant.now())
        }
}
