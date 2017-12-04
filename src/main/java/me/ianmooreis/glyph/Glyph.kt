package me.ianmooreis.glyph

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.OnlineStatus

object Glyph : JDABuilder(AccountType.BOT) {
    init {
        this.setToken(System.getenv("DISCORD_TOKEN"))
        this.setStatus(OnlineStatus.ONLINE).setGame(Game.playing("Armax Arsenal Arena"))
        this.addEventListener(MessageOrchestrator)
        this.addEventListener(ServerOrchestrator)
    }
}

fun main(args: Array<String>) {
    DatabaseOrchestrator.test()
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}