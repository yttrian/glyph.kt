package me.ianmooreis.glyph.extensions

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

fun User.getInfoEmbed(title: String?, footer: String?, color: Color?, showExactCreationDate: Boolean = false, mutualGuilds: Boolean = false): MessageEmbed {
    val botTag = if (this.isBot) "(bot)" else ""
    val createdAgo = PrettyTime().format(this.creationTime.toDate())
    return EmbedBuilder().setTitle(title)
            .setDescription(
                "**User** ${this.asPlainMention} $botTag\n" +
                "**ID** ${this.id}\n" +
                "**Mention** ${this.asMention}\n" +
                "**Created** $createdAgo ${if (showExactCreationDate) "(${this.creationTime})" else ""}" +
                if (mutualGuilds) "\n**Server** ${this.mutualGuilds.size} mutual" else "")
            .setThumbnail(this.avatarUrl)
            .setFooter(footer, null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build()
}

val User.asPlainMention
    get() = "${this.name}#${this.discriminator}"

val Member.asPlainMention
    get() = this.user.asPlainMention

val User.isCreator
    get() = this.idLong == System.getenv("CREATOR_ID").toLong()