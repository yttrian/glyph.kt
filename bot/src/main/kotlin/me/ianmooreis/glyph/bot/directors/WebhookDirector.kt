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
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import me.ianmooreis.glyph.bot.Director
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.TextChannel
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

/**
 * Manages sending webhook messages
 */
object WebhookDirector : Director() {
    private const val DEFAULT_VALUE = "?"

    /**
     * Attempts to convert a MessageEmbed into a WebhookEmbed
     */
    private fun MessageEmbed.toWebhookEmbed(): WebhookEmbed {
        val builder = WebhookEmbedBuilder()

        this.author?.let {
            val author = WebhookEmbed.EmbedAuthor(it.name ?: DEFAULT_VALUE, it.iconUrl, it.url)
            builder.setAuthor(author)
        }

        this.title?.let {
            val title = WebhookEmbed.EmbedTitle(it, this.url)
            builder.setTitle(title)
        }

        this.color?.let { builder.setColor(it.rgb) }

        this.description?.let { builder.setDescription(it) }

        this.image?.let { builder.setImageUrl(it.url) }

        this.thumbnail?.let { builder.setThumbnailUrl(it.url) }

        this.timestamp?.let { builder.setTimestamp(it) }

        this.footer?.let {
            val footer = WebhookEmbed.EmbedFooter(it.text ?: DEFAULT_VALUE, it.iconUrl)
            builder.setFooter(footer)
        }

        this.fields.forEach {
            val field = WebhookEmbed.EmbedField(it.isInline, it.name ?: DEFAULT_VALUE, it.value ?: DEFAULT_VALUE)
            builder.addField(field)
        }

        return builder.build()
    }

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
        getWebhookClient(channel) { client, base ->
            client.send(base.addEmbeds(embed.toWebhookEmbed()).build())
        }
    }

    private fun getWebhookClient(channel: TextChannel, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val selfUser = channel.jda.selfUser
        val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
        val key = channel.id
        val existingClient: WebhookClient? = cachedClients[key]

        // If there's no client cached, make one
        if (existingClient == null) {
            channel.retrieveWebhooks().queue { webhooks ->
                // If the channel has no webhooks then create one, otherwise steal one
                if (webhooks.isEmpty()) {
                    channel.createWebhook(selfUser.name).queue { webhook ->
                        val newClient = WebhookClientBuilder(webhook.url).build()
                        cachedClients[key] = newClient

                        success(newClient, baseMessage)
                    }
                } else {
                    val newClient = WebhookClientBuilder(webhooks.first().url).build()
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