package me.ianmooreis.glyph

import me.ianmooreis.glyph.orchestrators.*
import me.ianmooreis.glyph.skills.*
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game

object Glyph : JDABuilder(AccountType.BOT) {
    val version: String = System.getenv("HEROKU_RELEASE_VERSION")
    init {
        this.setToken(System.getenv("DISCORD_TOKEN"))
                .setStatus(OnlineStatus.ONLINE).setGame(Game.watching("Armax Arsenal Arena"))
                .addEventListener(MessageOrchestrator)
                .addEventListener(Auditing)
                .addEventListener(Server)
                .addEventListener(StatusOrchestrator)
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
            .addSkill(WikiSkill)
            .addSkill(TimeSkill)
            .addSkill(FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}