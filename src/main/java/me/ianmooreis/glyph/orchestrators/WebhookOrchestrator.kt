package me.ianmooreis.glyph.orchestrators

import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.SelfUser
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder

/**
 * Manages sending webhook messages
 */
object WebhookOrchestrator {

    /**
     * Send a webhook as self
     *
     * @param selfUser the self user
     * @param webhookUrl the webhook to send to
     * @param embed the embed to send
     */
    fun send(selfUser: SelfUser, webhookUrl: String, embed: MessageEmbed) {
        getWebhookClient(selfUser.name, selfUser.avatarUrl, webhookUrl) { client, base ->
            client.send(base.addEmbeds(embed).build())
        }
    }

    /**
     * Send a webhook with a custom name and avatar
     *
     * @param name the username to use
     * @param avatarUrl the avatar to use
     * @param webhookUrl the webhook to send to
     * @param embed the embed to send
     */
    fun send(name: String, avatarUrl: String?, webhookUrl: String, embed: MessageEmbed) {
        getWebhookClient(name, avatarUrl, webhookUrl) { client, base ->
            client.send(base.addEmbeds(embed).build())
        }
    }

    /**
     * Send a webhook to a guild's auditing webhook (if it has one)
     *
     * @param guild the guild to send the webhook message to
     * @param embed the embed to send
     */
    fun send(guild: Guild, embed: MessageEmbed) {
        getWebhookClient(guild) { client, base ->
            client.send(base.addEmbeds(embed).build())
        }
    }

    private fun getWebhookClient(guild: Guild, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val webhookUrl = guild.config.auditing.webhook
        if (webhookUrl != null) {
            val selfUser = guild.jda.selfUser
            val client = WebhookClientBuilder(webhookUrl).build()
            val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
            success(client, baseMessage)
            client.close()
        }
    }

    private fun getWebhookClient(name: String, avatarUrl: String?, webhookUrl: String, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val client = WebhookClientBuilder(webhookUrl).build()
        val baseMessage = WebhookMessageBuilder().setUsername(name).setAvatarUrl(avatarUrl)
        success(client, baseMessage)
        client.close()
    }
}