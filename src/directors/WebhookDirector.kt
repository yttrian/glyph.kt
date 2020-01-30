/*
 * WebhookDirector.kt * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

/**
 * Manages sending webhook messages
 */
object WebhookDirector : Director() {
    /**
     * Cache webhook clients so we don't continuously recreate them (like when a lot of people leave a server)
     * and can obey rate limits by reusing a client. Though, don't keep them forever because memory.
     */
    private val cachedClients: MutableMap<String, WebhookClient> = ExpiringMap.builder()
        .expiration(10, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expirationListener<String, WebhookClient> { _, client ->
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
     * Send a webhook to a channel
     *
     * @param channel the channel to send the webhook message to
     * @param embed the embed to send
     */
    fun send(channel: TextChannel, embed: MessageEmbed) {
        getWebhookClient(channel) { client, base ->
            client.send(base.addEmbeds(embed).build())
        }
    }

    private fun getWebhookClient(channel: TextChannel, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val selfUser = channel.jda.selfUser
        val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
        val key = channel.id
        val existingClient: WebhookClient? = cachedClients[key]

        // If there's no client cached, make one
        if (existingClient == null) {
            channel.webhooks.queue { webhooks ->
                // If the channel has no webhooks then create one, otherwise steal one
                if (webhooks.isEmpty()) {
                    channel.createWebhook(selfUser.name).queue { webhook ->
                        val newClient = WebhookClientBuilder(webhook).build()
                        cachedClients[key] = newClient

                        success(newClient, baseMessage)
                    }
                } else {
                    val newClient = WebhookClientBuilder(webhooks.first()).build()
                    cachedClients[key] = newClient

                    success(newClient, baseMessage)
                }
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
            WebhookClientBuilder(webhookUrl).build()
        }
        val baseMessage = WebhookMessageBuilder().setUsername(name).setAvatarUrl(avatarUrl)
        success(client, baseMessage)
    }
}