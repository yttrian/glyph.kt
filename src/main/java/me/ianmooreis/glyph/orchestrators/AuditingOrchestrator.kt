package me.ianmooreis.glyph.orchestrators

import me.ianmooreis.glyph.extensions.audit
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.getInfoEmbed
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color

/**
 * Manages auditing logs for servers
 */
object AuditingOrchestrator : ListenerAdapter() {

    /**
     * When a member joins a guild
     */
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.config.auditing.joins) {
            val embed = event.user.getInfoEmbed("Member Joined", "Auditing", Color.GREEN)
            event.guild.audit(embed.title, embed.description, embed.color)
        }
    }

    /**
     * When a member leaves a guild
     */
    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        if (event.guild.config.auditing.leaves) {
            val embed = event.user.getInfoEmbed("Member Left", "Auditing", Color.RED)
            event.guild.audit(embed.title, embed.description, embed.color)
        }
    }

    /**
     * When messages are bulk deleted
     */
    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        if (event.guild.config.auditing.purge) {
            event.guild.audit("Purge", "${event.messageIds.size} messages deleted in ${event.channel.asMention}", Color.YELLOW)
        }
    }

    /**
     * When a user updates their name
     */
    override fun onUserUpdateName(event: UserUpdateNameEvent) {
        val description = SimpleDescriptionBuilder()
            .addField("Old", event.oldName)
            .addField("New", event.newName)
            .addField("Mention", event.user.asMention)
            .addField("ID", event.user.id)
            .build()
        event.user.mutualGuilds.filter { it.config.auditing.names }.forEach { guild ->
            guild.audit("Name Change", description, Color.orange)
        }
    }
}