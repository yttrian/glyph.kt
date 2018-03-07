package me.ianmooreis.glyph

import me.ianmooreis.glyph.orchestrators.*
import me.ianmooreis.glyph.skills.*
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder

object Glyph : JDABuilder(AccountType.BOT) {
    val version: String = System.getenv("HEROKU_RELEASE_VERSION")
    init {
        this.setToken(System.getenv("DISCORD_TOKEN"))
                .addEventListener(MessageOrchestrator)
                .addEventListener(AuditingOrchestrator)
                .addEventListener(ServerOrchestrator)
                .addEventListener(StatusOrchestrator)
    }
}

fun main(args: Array<String>) {
    SkillOrchestrator
            .addSkill(HelpSkill)
            .addSkill(InfoSkill)
            .addSkill(RoleSetSkill).addSkill(RoleUnsetSkill).addSkill(RoleListSkill)
            .addSkill(ServerConfigGetSkill).addSkill(ServerConfigSetSkill)
            .addSkill(PurgeSkill)
            .addSkill(UserInfoSkill)
            .addSkill(RedditSkill)
            .addSkill(WikiSkill)
            .addSkill(TimeSkill)
            .addSkill(FeedbackSkill)
            .addSkill(FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}