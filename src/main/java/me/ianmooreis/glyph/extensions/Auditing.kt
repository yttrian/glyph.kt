package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.WebhookOrchestrator
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.SelfUser
import java.awt.Color
import java.time.Instant

/**
 * Sends an audit embed to a guild's auditing webhook (if it has one)
 *
 * @param title the title of the embed
 * @param description the body of the embed
 * @param color the color of the embed
 */
fun Guild.audit(title: String, description: String, color: Color? = null) {
    WebhookOrchestrator.send(this,
        EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setFooter("Auditing", null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build())
}

/**
 * Sends an embed to the global log webhook
 *
 * @param title the title of the embed
 * @param description the body of the embed
 * @param color the color of the embed
 */
fun SelfUser.log(title: String, description: String, color: Color? = null) {
    WebhookOrchestrator.send(this, System.getenv("LOGGING_WEBHOOK"),
        EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setFooter("Logging", null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build())
}

/**
 * Sends an embed to the global log webhook
 *
 * @param embed the embed to send
 */
fun SelfUser.log(embed: MessageEmbed) {
    WebhookOrchestrator.send(this, System.getenv("LOGGING_WEBHOOK"), embed)
}