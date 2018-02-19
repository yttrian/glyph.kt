package me.ianmooreis.glyph

import me.ianmooreis.glyph.orchestrators.AuditingOrchestrator
import me.ianmooreis.glyph.orchestrators.MessageOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerOrchestrator
import me.ianmooreis.glyph.orchestrators.SkillOrchestrator
import me.ianmooreis.glyph.skills.*
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game

object Glyph : JDABuilder(AccountType.BOT) {
    init {
        this.setToken(System.getenv("DISCORD_TOKEN"))
                .setStatus(OnlineStatus.ONLINE).setGame(Game.playing("Armax Arsenal Arena"))
                .addEventListener(MessageOrchestrator)
                .addEventListener(AuditingOrchestrator)
                .addEventListener(ServerOrchestrator)
    }
}

fun main(args: Array<String>) {
    SkillOrchestrator
            .addSkill(HelpSkill)
            .addSkill(InfoSkill)
            .addSkill(RoleSetSkill)
            .addSkill(RoleListSkill)
            .addSkill(ServerConfigGetSkill)
            .addSkill(ServerConfigSetSkill)
            .addSkill(PurgeSkill)
            .addSkill(UserInfoSkill)
            .addSkill(RedditSkill)
            .addSkill(FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}