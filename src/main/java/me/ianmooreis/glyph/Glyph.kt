package me.ianmooreis.glyph

import me.ianmooreis.glyph.orchestrators.*
import me.ianmooreis.glyph.skills.*
import me.ianmooreis.glyph.skills.configuration.ServerConfigGetSkill
import me.ianmooreis.glyph.skills.configuration.ServerConfigSetSkill
import me.ianmooreis.glyph.skills.moderation.BanSkill
import me.ianmooreis.glyph.skills.moderation.KickSkill
import me.ianmooreis.glyph.skills.moderation.PurgeSkill
import me.ianmooreis.glyph.skills.moderation.UserInfoSkill
import me.ianmooreis.glyph.skills.roles.RoleListSkill
import me.ianmooreis.glyph.skills.roles.RoleSetSkill
import me.ianmooreis.glyph.skills.roles.RoleUnsetSkill
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder

object Glyph : JDABuilder(AccountType.BOT) {
    val version: String = System.getenv("HEROKU_RELEASE_VERSION")
    init {
        this.setToken(System.getenv("DISCORD_TOKEN")).addEventListener(
                MessagingOrchestrator, AuditingOrchestrator, ServerOrchestrator, StatusOrchestrator, StarboardOrchestrator)
    }
}

fun main(args: Array<String>) {
    SkillOrchestrator.addSkill(
            HelpSkill, InfoSkill,
            RoleSetSkill, RoleUnsetSkill, RoleListSkill,
            ServerConfigGetSkill, ServerConfigSetSkill,
            PurgeSkill, UserInfoSkill, KickSkill, BanSkill,
            EphemeralSaySkill, RedditSkill, WikiSkill, TimeSkill, FeedbackSkill, DoomsdayClockSkill, SnowstampSkill,
            FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}