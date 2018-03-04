package me.ianmooreis.glyph.utils.webhooks

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.SelfUser
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import java.awt.Color
import java.time.Instant

object LoggingWebhook {

    fun log(event: GuildJoinEvent) {
        getWebhookClient(event.jda.selfUser, System.getenv("LOGGING_WEBHOOK")) { client, base ->
            client.send(base.addEmbeds(getGuildEmbed(event.guild).setTitle("Guild Joined").setColor(Color.GREEN).build()).build())
        }
    }

    fun log(event: GuildLeaveEvent) {
        getWebhookClient(event.jda.selfUser, System.getenv("LOGGING_WEBHOOK")) { client, base ->
            client.send(base.addEmbeds(getGuildEmbed(event.guild).setTitle("Guild Left").setColor(Color.RED).build()).build())
        }
    }

    fun log(exception: Exception, selfUser: SelfUser) {
        getWebhookClient(selfUser, System.getenv("LOGGING_WEBHOOK")) { client, base ->
            client.send(base.addEmbeds(getExceptionEmbed(exception)).build())
        }
    }

    fun log(title: String, description: String, selfUser: SelfUser) {
        getWebhookClient(selfUser, System.getenv("LOGGING_WEBHOOK")) { client, base ->
            client.send(base.addEmbeds(EmbedBuilder()
                    .setTitle(title).setDescription(description).setFooter("Logging", null)
                    .setTimestamp(Instant.now()).build()).build())
        }
    }

    private fun getExceptionEmbed(exception: Exception): MessageEmbed {
        return EmbedBuilder()
                .setTitle("Exception")
                .addField("Message", exception.message, false)
                .addField("Cause", exception.cause.toString(), false)
                .setColor(Color.RED)
                .setFooter("Logging", null)
                .setTimestamp(Instant.now())
                .build()
    }

    private fun getGuildEmbed(guild: Guild): EmbedBuilder {
        return EmbedBuilder().setDescription(
                "**Name** ${guild.name}\n" +
                "**ID** ${guild.id}\n" +
                "**Members** ${guild.members.size} (Bots: ${guild.members.count { it.user.isBot }})")
                .setThumbnail(guild.iconUrl)
                .setFooter("Logging", null)
                .setTimestamp(Instant.now())
    }

    private fun getWebhookClient(selfUser: SelfUser, url: String, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val client = WebhookClientBuilder(url).build()
        val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
        success(client, baseMessage)
        client.close()
    }
}