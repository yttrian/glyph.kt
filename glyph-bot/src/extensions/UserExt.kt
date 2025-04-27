package org.yttr.glyph.bot.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
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
fun User.getInfoEmbed(
    title: String?,
    footer: String?,
    color: Color?,
    showExactCreationDate: Boolean = false,
    mutualGuilds: Boolean = false
): MessageEmbed {
    val botTag = if (this.isBot) "(bot)" else ""
    val createdAgo = PrettyTime().format(this.timeCreated.toDate())
    val descriptionBuilder: SimpleDescriptionBuilder = SimpleDescriptionBuilder()
        .addField("User", "${this.asPlainMention} $botTag")
        .addField("ID", this.id)
        .addField("Mention", this.asMention)
        .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.timeCreated})" else ""}")
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
    get() = when {
        isBot -> "${this.name}#${this.discriminator}"
        else -> this.name
    }

/**
 * Gets a string of the username with the discriminator
 */
val Member.asPlainMention: String
    get() = this.user.asPlainMention

/**
 * Reports if a user if the creator
 */
val User.isCreator: Boolean
    get() = this.idLong == Glyph.conf.getLong("management.creator-id")

/**
 * Send a user a PM before an action where they might not be seen again (kick/ban)
 */
fun User.sendDeathPM(message: String, die: () -> Unit) {
    if (!this.isBot) {
        this.openPrivateChannel().queue { pm ->
            pm.sendMessage(message).queue({
                pm.close().queue {
                    die()
                }
            }, { die() })
        }
    } else {
        die()
    }
}
