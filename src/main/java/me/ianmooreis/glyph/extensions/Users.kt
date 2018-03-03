package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.CustomEmote
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

fun User.getinfoEmbed(title: String?, footer: String?, color: Color?): MessageEmbed {
    val botTag = if (this.isBot) CustomEmote.BOT.toString() else ""
    val createdAgo = PrettyTime().format(this.creationTime.toDate())
    return EmbedBuilder().setTitle(title)
            .setDescription(
                "**User** ${this.name}#${this.discriminator} $botTag\n" +
                "**ID** ${this.id}\n" +
                "**Mention** ${this.asMention}\n" +
                "**Created** $createdAgo")
            .setThumbnail(this.avatarUrl)
            .setFooter(footer, null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build()
}
