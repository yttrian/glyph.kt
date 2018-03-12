package me.ianmooreis.glyph.orchestrators

import me.ianmooreis.glyph.extensions.audit
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.getInfoEmbed
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color

object AuditingOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.config.auditing.joins) {
            val embed = event.user.getInfoEmbed("Member Joined", "Auditing", Color.GREEN)
            event.guild.audit(embed.title, embed.description, embed.color)
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        if (event.guild.config.auditing.leaves) {
            val embed = event.user.getInfoEmbed("Member Left", "Auditing", Color.RED)
            event.guild.audit(embed.title, embed.description, embed.color)
        }
    }

    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        if(event.guild.config.auditing.purge) {
            event.guild.audit("Purge", "${event.messageIds.size} messages deleted in ${event.channel.asMention}", Color.YELLOW)
        }
    }
}