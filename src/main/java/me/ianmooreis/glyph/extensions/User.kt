package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

/**
 * Get an informational embed about a user
 *
 * @param title  the title of the embed
 * @param footer any footer text to include in the embed
 * @param color  the color of the embed
 * @param showExactCreationDate whether or not to show the exact timestamp for the user's creation time
 * @param mutualGuilds whether or not to show how many guilds are shared with the user
 *
 * @return an embed with the requested user info
 */
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

/**
 * Gets a string of the username with the discriminator
 */
val User.asPlainMention: String
    get() = "${this.name}#${this.discriminator}"

/**
 * Gets a string of the username with the discriminator
 */
val Member.asPlainMention: String
    get() = this.user.asPlainMention

/**
 * Reports if a user if the creator
 */
val User.isCreator: Boolean
    get() = this.idLong == System.getenv("CREATOR_ID").toLong()