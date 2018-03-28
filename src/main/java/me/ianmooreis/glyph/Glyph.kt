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
        this.setToken(System.getenv("DISCORD_TOKEN"))
                .addEventListener(MessagingOrchestrator)
                .addEventListener(AuditingOrchestrator)
                .addEventListener(ServerOrchestrator)
                .addEventListener(StatusOrchestrator)
                .addEventListener(StarboardOrchestrator)
    }
}

fun main(args: Array<String>) {
    SkillOrchestrator
            .addSkill(HelpSkill)
            .addSkill(InfoSkill)
            .addSkill(RoleSetSkill).addSkill(RoleUnsetSkill).addSkill(RoleListSkill)
            .addSkill(ServerConfigGetSkill).addSkill(ServerConfigSetSkill)
            .addSkill(PurgeSkill).addSkill(UserInfoSkill).addSkill(KickSkill).addSkill(BanSkill)
            .addSkill(EphemeralSaySkill)
            .addSkill(RedditSkill)
            .addSkill(WikiSkill)
            .addSkill(TimeSkill)
            .addSkill(FeedbackSkill)
            .addSkill(DoomsdayClockSkill)
            .addSkill(FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}