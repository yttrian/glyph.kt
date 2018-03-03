package me.ianmooreis.glyph.orchestrators

import me.ianmooreis.glyph.extensions.getinfoEmbed
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color

object Auditing : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.config.auditingJoins) {
            this.getWebhookClient(event.guild) { client, base ->
                client.send(base.addEmbeds(event.user.getinfoEmbed("Member Join", "Auditing", Color.GREEN)).build())
            }
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        if (event.guild.config.auditingLeaves) {
            this.getWebhookClient(event.guild) { client, base ->
                client.send(base.addEmbeds(event.user.getinfoEmbed("Member Leave", "Auditing", Color.RED)).build())
            }
        }
    }


    private fun getWebhookClient(guild: Guild, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val webhookUrl = guild.config.auditingWebhook
        if (webhookUrl != null) {
            val selfUser = guild.jda.selfUser
            val client = WebhookClientBuilder(webhookUrl).build()
            val baseMessage = WebhookMessageBuilder().setUsername(selfUser.name).setAvatarUrl(selfUser.avatarUrl)
            success(client, baseMessage)
            client.close()
        }
    }
}