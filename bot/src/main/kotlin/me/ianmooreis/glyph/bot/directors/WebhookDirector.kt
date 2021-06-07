/*
 * WebhookDirector.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ianmooreis.glyph.bot.directors

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.receive.ReadonlyMessage
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.ianmooreis.glyph.bot.Director
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.WebhookType
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

/**
 * Manages sending webhook messages
 */
object WebhookDirector : Director() {
    private const val CLIENT_CACHE_EXPIRATION_MINUTES = 10L

    /**
     * Convert a MessageEmbed into a WebhookEmbed
     */
    private fun MessageEmbed.toWebhookEmbed(): WebhookEmbed = WebhookEmbedBuilder.fromJDA(this).build()

    /**
     * Cache webhook clients so we don't continuously recreate them (like when a lot of people leave a server)
     * and can obey rate limits by reusing a client. Though, don't keep them forever because memory.
     */
    private val cachedClients: MutableMap<String, JDAWebhookClient> = ExpiringMap.builder()
        .expiration(CLIENT_CACHE_EXPIRATION_MINUTES, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expirationListener<String, JDAWebhookClient> { _, client ->
            client.close()
        }
        .build()

    /**
     * Send a webhook as self
     *
     * @param selfUser the self user
     * @param webhookUrl the webhook to send to
     * @param embed the embed to send
     */
    fun send(selfUser: SelfUser, webhookUrl: String, embed: MessageEmbed) {
        getWebhookClient(selfUser.name, selfUser.avatarUrl, webhookUrl) { client, base ->
            client.send(base.addEmbeds(embed.toWebhookEmbed()).build())
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
            client.send(base.addEmbeds(embed.toWebhookEmbed()).build())
        }
    }

    /**
     * Send a webhook to a channel
     *
     * @param channel the channel to send the webhook message to
     * @param embed the embed to send
     */
    fun send(channel: TextChannel, embed: MessageEmbed) {
        launch {
            getWebhookClient(channel) { client, base ->
                client.send(base.addEmbeds(embed.toWebhookEmbed()).build())
            }
        }
    }

    /**
     * Send a webhook to a channel and get the sent message data
     *
     * @param channel the channel to send the webhook message to
     * @param message the message to send
     */
    suspend fun send(channel: TextChannel, message: Message): ReadonlyMessage {
        return getWebhookClient(channel) { client, base ->
            base.setContent(message.contentRaw)
            base.addEmbeds(message.embeds.map { it.toWebhookEmbed() })
            client.send(base.build())
        }.await()
    }

    /**
     * Send a webhook to a channel
     *
     * @param channel the channel to send the webhook message to
     * @param webhookMessageId the webhook message to update
     * @param message the message to send
     */
    suspend fun update(channel: TextChannel, webhookMessageId: Long, message: Message): ReadonlyMessage {
        return getWebhookClient(channel) { client, base ->
            base.setContent(message.contentRaw)
            base.addEmbeds(message.embeds.map { it.toWebhookEmbed() })
            client.edit(webhookMessageId, base.build())
        }.await()
    }

    private suspend fun <T> getWebhookClient(
        channel: TextChannel,
        success: (JDAWebhookClient, WebhookMessageBuilder) -> T
    ): T {
        val selfUser = channel.jda.selfUser
        val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
        val key = channel.id
        val existingClient: JDAWebhookClient? = cachedClients[key]

        // If there's no client cached, make one
        return if (existingClient == null) {
            val webhooks = channel.retrieveWebhooks().await()
            val stealableWebhook = webhooks.find { it.type == WebhookType.INCOMING && !it.isFake }
            // If the channel has no webhooks then create one, otherwise steal one
            if (stealableWebhook != null) {
                val newClient = WebhookClientBuilder(stealableWebhook.url).buildJDA()
                cachedClients[key] = newClient

                success(newClient, baseMessage)
            } else {
                val webhook = channel.createWebhook(selfUser.name).await()
                val newClient = WebhookClientBuilder(webhook.url).buildJDA()
                cachedClients[key] = newClient

                success(newClient, baseMessage)
            }
        } else {
            success(existingClient, baseMessage)
        }
    }

    private fun getWebhookClient(
        name: String,
        avatarUrl: String?,
        webhookUrl: String,
        success: (WebhookClient, WebhookMessageBuilder) -> Unit
    ) {
        val client = cachedClients.getOrPut(webhookUrl) {
            WebhookClientBuilder(webhookUrl).buildJDA()
        }
        val baseMessage = WebhookMessageBuilder().setUsername(name).setAvatarUrl(avatarUrl)
        success(client, baseMessage)
    }
}
