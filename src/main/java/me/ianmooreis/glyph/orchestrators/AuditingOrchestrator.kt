package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

object AuditingOrchestrator : ListenerAdapter() {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent?) {
        super.onGuildMemberJoin(event)
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent?) {
        super.onGuildMemberLeave(event)
    }
}