package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.entities.Webhook
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import org.ocpsoft.prettytime.PrettyTime
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.time.Instant

object AuditingOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.config.auditingJoins) {
            val selfUser = event.jda.selfUser
            this.getWebhookClient(event.guild) { client ->
                client.send(WebhookMessageBuilder()
                        .setUsername(selfUser.name)
                        .setAvatarUrl(selfUser.avatarUrl)
                        .addEmbeds(this.getUserInfoEmbed(event.user)
                                .setTitle("Member Join")
                                .setColor(Color.GREEN)
                                .build())
                        .build())
            }
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        if (event.guild.config.auditingLeaves) {
            val selfUser = event.jda.selfUser
            this.getWebhookClient(event.guild) { client ->
                client.send(WebhookMessageBuilder()
                        .setUsername(selfUser.name)
                        .setAvatarUrl(selfUser.avatarUrl)
                        .addEmbeds(this.getUserInfoEmbed(event.user)
                                .setTitle("Member Leave")
                                .setColor(Color.RED)
                                .build())
                        .build())
            }
        }
    }

    private fun getUserInfoEmbed(user: User): EmbedBuilder {
        val botTag: String = if (user.isBot) CustomEmote.BOT.toString() else ""
        val createdAgo = PrettyTime().format(user.creationTime.toDate())
        return EmbedBuilder().setDescription(
                "**User** ${user.name}#${user.discriminator} $botTag\n" +
                        "**ID** ${user.id}\n" +
                        "**Mention** ${user.asMention}\n" +
                        "**Created** $createdAgo")
                .setThumbnail(user.avatarUrl)
                .setFooter("Moderation", null)
                .setTimestamp(Instant.now())
    }

    private fun getWebhookClient(guild: Guild, success: (WebhookClient) -> Unit) {
        if (!guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
            this.log.warn("Missing Manage Webhooks permission in guild $guild!")
            return
        }
        this.getWebhook(guild) {
            val client = it.newClient().build()
            success(client)
            client.close()
        }
    }

    private fun getWebhook(guild: Guild, success: (Webhook) -> Unit) {
        if (!guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
            this.log.warn("Missing Manage Webhooks permission in guild $guild!")
            return
        }
        guild.webhooks.queue { webhooks ->
            val webhook = webhooks.find { it.name == "Glyph" }
            val channel = guild.getTextChannelsByName(guild.config.auditingChannel, true).getOrNull(0)
            if (webhook != null) {
                if (webhook.channel.name != guild.config.auditingChannel && channel != null) {
                    webhook.manager.setChannel(channel).reason("Change in config").queue()
                }
                success(webhook)
            } else if (channel != null) {
                if (guild.selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
                    channel.createWebhook("Glyph").reason("For auditing").queue { success(it) }
                } else {
                    this.log.warn("Missing Manage Webhooks permission in channel $channel")
                }
            } else {
                this.log.warn("Failed to get or create webhook in guild $guild!")
            }
        }
    }
}