package org.yttr.glyph.bot.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfUser
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.messaging.WebhookDirector
import java.awt.Color
import java.time.Instant

/**
 * Sends an embed to the global log webhook
 *
 * @param title the title of the embed
 * @param description the body of the embed
 * @param color the color of the embed
 */
fun SelfUser.log(title: String, description: String, color: Color? = null) {
    WebhookDirector.send(
        this, Glyph.conf.getString("management.logging-webhook"),
        EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setFooter("Logging", null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build()
    )
}

/**
 * Sends an embed to the global log webhook
 *
 * @param embed the embed to send
 */
fun SelfUser.log(embed: MessageEmbed) {
    WebhookDirector.send(this, Glyph.conf.getString("management.logging-webhook"), embed)
}
