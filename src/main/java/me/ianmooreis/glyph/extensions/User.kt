package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
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
    val descriptionBuilder: SimpleDescriptionBuilder = SimpleDescriptionBuilder()
            .addField("User", "${this.asPlainMention} $botTag")
            .addField("ID", this.id)
            .addField("Mention", this.asMention)
            .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.creationTime})" else ""}")
    if (mutualGuilds) {
        descriptionBuilder.addField("Server", "${this.mutualGuilds.size} mutual")
    }
    return EmbedBuilder().setTitle(title)
            .setDescription(descriptionBuilder.build())
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