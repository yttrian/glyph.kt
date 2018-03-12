package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.WebhookOrchestrator
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.SelfUser
import java.awt.Color
import java.time.Instant

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

fun Guild.audit(embed: MessageEmbed) {
    WebhookOrchestrator.send(this, embed)
}

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