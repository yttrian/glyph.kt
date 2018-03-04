package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.*
import kotlin.concurrent.schedule


object StatusOrchestrator : ListenerAdapter() {
    private val statuses = listOf(
            Game.playing("Armax Arsenal Arena"),
            Game.watching("Armax Arsenal Arena"),
            Game.listening("the hum of the ship"),
            Game.playing("Mass Effect"))

    override fun onReady(event: ReadyEvent) {
        super.onReady(event)
        ServerOrchestrator.updateServerCount(event.jda)
        Timer().schedule(600000) {
            event.jda?.presence?.setPresence(OnlineStatus.ONLINE, getRandomStatus())
        }
    }

    fun setStatus(jda: JDA, status: OnlineStatus = OnlineStatus.ONLINE, game: Game) {
        jda.presence.setPresence(status, game)
    }

    private fun getRandomStatus(): Game {
        return statuses[Random().nextInt(statuses.size)]
    }
}